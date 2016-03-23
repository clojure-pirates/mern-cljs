(ns api.handlers
  (:require-macros
    [mern-utils.macros :refer [node-require, node-config defroute]])
  (:require
    [cljs.nodejs :as nodejs]
    [mern-utils.amqp :refer [send-to-queue amqp-state]]))

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
    ((.authenticate
       passport
       "local-login"
       (clj->js {:session true})
       (fn [err user info]
         (if err
           (println (str "user error: " err))
           (if (not user)
             (println (str "user not found error: " info))
             (.logIn
               req
               user
               (fn [err]
                 (if err
                   (println (str "login error: " err))
                   (-> res
                       (.status 200)
                       (.json (clj->js {:message "OK" :sessionID (.-sessionID req)}))))))))))
     req res)))

(defroute task-handler "get" "/task"
  ; This is for showing how to trigger a async task
  (do (send-to-queue (:channel @amqp-state) (:default-queue @amqp-state) "hello")
      (-> res
        (.status 200)
        (.json (clj->js {:message "OK"})))))
