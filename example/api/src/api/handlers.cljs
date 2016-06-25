(ns api.handlers
  (:require-macros
    [mern-utils.macros :refer [node-require, node-config defroute]])
  (:require
    [clojure.string :as str]
    [cljs.nodejs :as nodejs]
    [cljs-time.core :as time-core]
    [cljs-time.coerce :as time-coerce]
    [cemerick.url :refer [url]]
    [mern-utils.lib :refer [str->hex raise]]
    [mern-utils.backend-lib :refer [get-url-params log]]
    [mern-utils.amqp :refer [queue-task amqp-state]]
    [mern-utils.db :as db]
    [cljs-hash.md5  :refer [md5]]
    [common.config :refer [LOGGER]]
    ))

(defonce route-table (atom []))  ; defroute macro needs this
(node-require express "express")
(node-require passport "passport")

(defroute homepage-handler "get" "/"
  (-> res
      (.status 403)
      (.json (clj->js {:error "Not allowed"}))))

(defn user-profile [req]
  (let [profile (.-user req)]
    ; TODO: Return only safe info
    profile))

(defroute me-handler "get" "/me"
  (do
    (if (true? (-> req (.isAuthenticated)))
    (-> res
        (.status 200)
        (.json (clj->js {:message "OK" :user (user-profile req)})))
    (-> res
        (.status 404)
        (.json (clj->js {:error "Not Found" :sessionID (.-sessionID req)}))))))

(defroute login-handler "post" "/login"
  (do
    (log LOGGER :debug "login invoked")
    ((.authenticate
       passport
       "local-login"
       (clj->js {:session true})
       (fn [err user info]
         (log LOGGER :debug (str "login user " user))
         (if err
           (-> res
             (.status 401)
             (.json (clj->js {:message "Login error"})))
           (if (not user)
             (-> res
                 (.status 401)
                 (.json (clj->js {:message "Authentication error"})))
             (.logIn
               req
               user
               (fn [err]
                 (if err
                   (log LOGGER :debug (str "login error" err))
                   (-> res
                       (.status 200)
                       (.json (clj->js {:message "OK" :sessionID (.-sessionID req)}))))))))))
     req res)))

(defroute task-handler "get" "/task"
  ; This is for showing how to trigger a async task
  (do (queue-task (:channel @amqp-state) (:default-queue @amqp-state) {:fn "log-message" :data {:message "hello"}})
      (-> res
        (.status 200)
        (.json (clj->js {:message "OK"})))))
