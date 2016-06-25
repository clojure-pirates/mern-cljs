(ns ^:figwheel-always app.core
  (:require
    [clojure.string :as string]
    [cljs.core.async :as async :refer [chan close! timeout put!]]
    [goog.events :as events]
    [reagent.core :as reagent]
    [app.atom :refer [app-state]]
    [mern-utils.frontend-lib :refer [get-js-var get-path get-url-params
                                     get-elem get-elems-by-tag-name]]
    [app.views :refer [header-view profile-view]]
    [app.controller :refer [login silent-login get-me refresh-profile show-flash
                            redirect-to-login set-login-next show-login-button]]))

(enable-console-print!)

(defn profile [target-elem]
  (login
    (fn [res]
      (if (= "OK" (:message res))
        (get-me (fn [res] (refresh-profile (:user res)))
                (fn [status message] (show-flash message)))
        (show-flash "Oops, something went wrong...")))
    "me"
    (fn [res] (redirect-to-login "me")))
  (reagent/render [#(profile-view @app-state)] target-elem))

(defn route [path]
  (let [content-elem (first (get-elems-by-tag-name "content"))
        header-elem (get-elem "header")
        menu-elem (get-elem "menu")
        page (second (string/split path "/"))]
    (case page
      "me"      (profile content-elem)
      (println "No handler defined for path: " path))))

(defn activate []
  (let [user-action (chan)]
    (swap! app-state assoc :url-params (get-url-params))
    (route (get-path))))
