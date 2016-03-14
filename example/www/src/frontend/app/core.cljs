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

(def LOGIN (str "http://" API-DOMAIN ":" API-PORT "/login"))
(def ME (str "http://" API-DOMAIN ":" API-PORT "/me"))

(defn login [uid token callback]
  (post-json
    LOGIN
    {:uid uid :token token}
    callback
    (fn [res] println "Login error:" res)))

(defn refresh-profile [res]
  (let [uid (.-userUID (js* "window"))
        token (.-shortTermToken (js* "window"))
        auth-strategy PRIMARY-SOCIAL-AUTH]
    (if (or (= 0 (:status res)) (= 500 (:status res)))
      (swap! app-state assoc
             :flash "Oops, a network problem..."
             :flash-shown true)
      (if (= 404 (:status res))
        (if (and (< 0 (count uid)) (< 0 (count token)))
          (login uid token
                 (fn [res]
                   (if (= "OK" (:message res))
                     (get-json ME refresh-profile refresh-profile)
                     (redirect "/error"))))
          (redirect (str "/auth/" auth-strategy "?next=profile")))
        (swap! app-state assoc
               :user (:user res)
               :flash ""
               :flash-shown false
               :profile-loading-shown false
               :profile-shown true
               :profile-greetings "You are awesome, ")))))

(defn profile [target-elem]
  (get-json ME refresh-profile refresh-profile)
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
