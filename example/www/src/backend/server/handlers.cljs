(ns server.handlers
  (:require-macros
    [mern-utils.macros :refer [node-require, node-config defroute]])
  (:require
    [cljs.nodejs :as nodejs]
    [clojure.string :as string]
    [cognitect.transit :as transit]
    [cemerick.url :refer (url url-encode)]
    [mern-utils.backend-lib :refer [set-next-url-from-param get-uid-token get-js-to-def-vars]]
    [mern-utils.express :refer [render]]
    [mern-utils.view :refer [render-page]]
    [server.views :refer [home-view login-view profile-view]]))

(defonce route-table (atom []))  ; defroute macro needs this
(node-require express "express")
(node-require passport "passport")

(defroute homepage-handler "get" "/"
  (let [data {:title "MERN-Cljs demo" :content (home-view)}]
    (render req res (render-page data))))

(defroute login-handler "get" "/login"
  (let [data {:title "MERN-Cljs demo - Login"
              :content (login-view)}]
    (do (println "Login invoked...")
        (set-next-url-from-param req res "profile")
        (println "  Next URL: " (.. req -cookies -nextUrl))
        (render req res (render-page data)))))

(defroute auth-handler "get" "/auth/*"
  (let [protocol (last (string/split (:path (url (.-originalUrl req))) "/"))]
    (println "Auth via" protocol "...")
    ((.authenticate passport protocol
                    (clj->js { :scope ["email"] }))
     req res (fn[req0 res0]
               (do (println "All fail: " req0 res0)
                   (.redirect res "/login"))))))

(defroute auth-callback-handler "get" "/authcb/*"
  (let [protocol (last (string/split (:path (url (.-originalUrl req))) "/"))
        next-url (or (.. req -cookies -nextUrl) "/")]
    (println protocol "callback invoked..." )
      ((.authenticate passport protocol
                      (clj->js {:successRedirect next-url
                                :failureRedirect "/"}))
       req res (fn[req0 res0]
                 (do (println "All fail: " req0 res0)
                     (.redirect res "/login"))))))

(defroute profile-handler "get" "/profile"
  (let [cred (get-uid-token req res)
        js-root-vars ["userUID" (:uid cred)
                      "shortTermToken" (:token cred)]
        script (get-js-to-def-vars js-root-vars)
        data {:title "MERN-Cljs - Profile"
              :content (profile-view)
              :scripts [script] }]
    (render req res (render-page data))))
