(ns app.controller
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]])
  (:require
    [clojure.string :as string]
    [cljs.core.async :as async :refer [chan close! timeout put!]]
    [goog.events :as events]
    [ajax.core :refer [GET]]
    [reagent.core :as reagent]
    [common.frontend-config :refer [API-DOMAIN API-PORT]]
    [mern-utils.frontend-lib :refer [get-js-var get-json post-json
                                     get-path get-url-params
                                     get-elems-by-tag-name redirect]]
    [mern-utils.lib :refer [set-timeout]]
    [app.atom :refer [app-state]]))

(enable-console-print!)

(def API-ENDPOINT (str "http://" API-DOMAIN ":" API-PORT))
(def LOGIN (str API-ENDPOINT "/login"))
(def ME (str API-ENDPOINT "/me"))

(defn redirect-to-login [next-]
  (redirect (str "/login/?next=" next-)))

(defn do-login [uid token then error]
  (post-json LOGIN {:uid uid :token token} then error))

(defn silent-login
  "Silently try logging in.
  - Don't redirect to the next page upon success.
  - Don't redirect to the auth page if fails."
  [then error]
  (let [uid (get-js-var "userUID")
        token (get-js-var "shortTermToken")]
    (if (and (< 0 (count uid)) (< 0 (count token)))
      (do-login uid token then (fn [res] (error)))
      (error))))

(defn login
  "Fetch uid and token and login. Redirect to social auth if fails"
  [then next- error]
  (let [uid (get-js-var "userUID")
        token (get-js-var "shortTermToken")]
    (if (and (< 0 (count uid)) (< 0 (count token)))
      (do-login uid token then (fn [res] (error)))
      (do (println "uid and token are not defined. Redirecting to social auth...")
          (redirect-to-login next-)))))

(defn set-login-next [next-]
  (swap! app-state assoc :login-next next-))

(defn get-me [then err]
  (get-json
    ME
    (fn [res]
      (case (:status res)
        (0 500) (err (:status res) "The API server is down...")
        404 (err (:status res) "Failed to fetch your information.")
        (then res)))
    (fn [res] (err (:status nil) "Oops, something went wrong while fetching info..."))))

(defn show-flash [message]
  (swap! app-state assoc
         :flash-message message
         :flash-shown true))

(defn refresh-profile [user]
  (swap! app-state assoc
         :user user
         :profile-greetings "You are awesome,"
         :flash ""
         :flash-shown false
         :profile-greetings-shown true
         :profile-login-button-shown false
         :profile-loading-shown false
         :profile-pic-shown true
         :menu-logout-shown false))

(defn show-login-button []
  (swap! app-state assoc
         :flash ""
         :flash-shown false
         :profile-login-button-shown true
         :profile-greetings-shown false
         :profile-loading-shown false
         :profile-pic-shown false))

(defn on-profile-login-button-click []
  (let [next-page (:login-next @app-state)]
    (login
      (fn [res]
        (if (= "OK" (:message res))
          (get-me (fn [res] (refresh-profile (:user res)))
                  (fn [status message] (show-flash message)))
          (show-flash "Oops, something went wrong...")))
      next-page
      (fn [res] (show-flash "Login error")))))
