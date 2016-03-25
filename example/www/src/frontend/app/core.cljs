(ns ^:figwheel-always app.core
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]])
  (:require
    [cljs.core.async :as async :refer [chan close! timeout put!]]
    [goog.events :as events]
    [common.config :refer [API-DOMAIN API-PORT PRIMARY-SOCIAL-AUTH]]
    [reagent.core :as reagent]
    [app.atom :refer [app-state]]
    [app.utils :refer [get-json post-json get-path get-url-params get-elems-by-tag-name redirect]]
    [app.views :refer [profile-view]]))

(enable-console-print!)

(def API-ENDPOINT (str "http://" API-DOMAIN ":" API-PORT))
(def LOGIN (str API-ENDPOINT "/login"))
(def ME (str API-ENDPOINT "/me"))

(defn show-flash [message]
  (swap! app-state assoc
         :flash message
         :flash-shown true))

(defn do-login [uid token then error]
  (post-json LOGIN {:uid uid :token token} then error))

(defn login
  "Fetch uid and token and login. Redirect to social auth if fails"
  [then]
  (let [uid (.-userUID (js* "window"))
        token (.-shortTermToken (js* "window"))
        auth-strategy PRIMARY-SOCIAL-AUTH]
    (if (and (< 0 (count uid)) (< 0 (count token)))
      (do-login uid token then (fn [res] (show-flash "Login error.")))
      (redirect (str "/auth/" auth-strategy "?next=profile")))))

(defn get-me [then]
  (get-json
    ME
    (fn [res]
      (case (:status res)
        (0 500) (show-flash "The API server is down...")
        404 (show-flash "Failed to fetch your information.")
        (then res)))
    (fn [res] (show-flash "Oops, something went wrong while fetching info..."))))

(defn refresh-profile [res]
  (swap! app-state assoc
         :user (:user res)
         :flash ""
         :flash-shown false
         :profile-loading-shown false
         :profile-shown true
         :profile-greetings "You are awesome, "))

(defn profile [target-elem]
  (login
    (fn [res]
      (if (= "OK" (:message res))
        (get-me refresh-profile)
        (show-flash "Oops, something went wrong..."))))
  (reagent/render [#(profile-view @app-state)] target-elem))

(defn route [path target-elem]
  (case path
    "/profile" (profile target-elem)
    (println "No handler defined for path: " path)))

(defn activate []
  (let [elem (first (get-elems-by-tag-name "content"))
        user-action (chan)]
    (swap! app-state assoc :url-params (get-url-params))
    (route (get-path) elem)))
