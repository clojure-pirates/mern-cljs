(ns ^:figwheel-always app.core
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]])
  (:require
    [clojure.string :as string]
    [cljs.core.async :as async :refer [chan close! timeout put!]]
    [goog.events :as events]
    [common.config :refer [API-DOMAIN API-PORT PRIMARY-SOCIAL-AUTH]]
    [reagent.core :as reagent]
    [app.atom :refer [app-state]]
    [mern-utils.frontend-lib :refer [get-js-var get-json post-json get-path
                                     get-url-params get-elems-by-tag-name
                                     redirect]]
    [app.views :refer [profile-view]]))

(enable-console-print!)

(def API-ENDPOINT (str "http://" API-DOMAIN ":" API-PORT))
(def LOGIN (str API-ENDPOINT "/login"))
(def ME (str API-ENDPOINT "/me"))

(defn show-flash [message]
  (swap! app-state assoc
         :flash-message message
         :flash-shown true))

(defn redirect-to-login [next-]
  (redirect (str "/login/?next=" next-)))

(defn do-login [uid token then error]
  (post-json LOGIN {:uid uid :token token} then error))

(defn login
  "Fetch uid and token and login. Redirect to social auth if fails"
  [then next- error]
  (let [uid (get-js-var "userUID")
        token (get-js-var "shortTermToken")]
    (if (and (< 0 (count uid)) (< 0 (count token)))
      (do-login uid token then (fn [res] (error)))
      (do (println "uid and token are not defined. Redirecting to social auth...")
          (redirect-to-login next-)))))

(defn get-me [then]
  (get-json
    ME
    (fn [res]
      (case (:status res)
        (0 500) (show-flash "The API server is down...")
        404 (show-flash "Failed to fetch your information.")
        (then res)))
    (fn [res] (show-flash "Oops, something went wrong while fetching info..."))))

(defn refresh-profile [user]
  (swap! app-state assoc
         :user user
         :profile-greetings "You are awesome,"
         :flash ""
         :flash-shown false
         :profile-greetings-shown true
         :profile-loading-shown false
         :profile-pic-shown true))

(defn profile [target-elem]
  (login
    (fn [res]
      (if (= "OK" (:message res))
        (get-me (fn [res] (refresh-profile (:user res))))
        (show-flash "Oops, something went wrong...")))
    "profile"
    (fn [res] (redirect-to-login "profile")))
  (reagent/render [#(profile-view @app-state)] target-elem))

(defn route [path target-elem]
  (let [page (second (string/split path "/"))]
    (case page
      "profile" (profile target-elem)
      (println "No handler defined for path: " path))))

(defn activate []
  (let [elem (first (get-elems-by-tag-name "content"))
        user-action (chan)]
    (swap! app-state assoc :url-params (get-url-params))
    (route (get-path) elem)))
