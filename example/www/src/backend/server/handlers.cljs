(ns server.handlers
  (:require-macros
    [mern-utils.macros :refer [node-require, node-config defroute]])
  (:require
    [cemerick.url :refer [url url-encode]]
    [cljs.nodejs :as nodejs]
    [clojure.string :as string]
    [clojure.walk :refer [keywordize-keys]]
    [mern-utils.backend-lib :refer [get-path get-url-params set-next-url-from-param
                                    get-uid-token get-js-to-def-vars log]]
    [mern-utils.db :as db]
    [mern-utils.express :refer [render]]
    [mern-utils.view :refer [render-page]]
    [common.config :refer [LOGGER WWW-SITE-TITLE WWW-EXTERNAL-DOMAIN DEFAULT-LOGIN-PAGE]]
    [server.views :refer [home-view login-view profile-view]]))

(defonce route-table (atom []))  ; defroute macro needs this
(node-require express "express")
(node-require passport "passport")

(defroute homepage-handler "get" "/"
  (let [data {:title WWW-SITE-TITLE
              :content (home-view)}]
    (render req res (render-page data))))

(defroute login-handler "get" "/login"
  (let [data {:title (str WWW-SITE-TITLE " - Login")
              :content (login-view)}]
    (do (set-next-url-from-param req res DEFAULT-LOGIN-PAGE)
        (log LOGGER :debug (str "Login invoked.  Next URL: " (.. req -cookies -nextUrl)))
        (render req res (render-page data)))))

(defroute auth-handler "get" "/auth/*"
  (let [protocol (last (string/split (get-path req) "/"))]
    (log LOGGER :debug (str "Auth via " protocol "..."))
    ((.authenticate passport protocol
                    (clj->js { :scope ["email"] }))
     req res (fn[req0 res0]
               (do (log LOGGER :info (str "Auth all fail: " req0 res0))
                   (.redirect res "/login"))))))

(defroute auth-callback-handler "get" "/authcb/*"
  (let [protocol (last (string/split (get-path req) "/"))
        next-url (or (.. req -cookies -nextUrl) "/")]
    (log LOGGER :debug (str protocol " callback invoked..."))
      ((.authenticate passport protocol
                      (clj->js {:successRedirect next-url
                                :failureRedirect "/"}))
       req res (fn[req0 res0]
                 (do (log LOGGER :info (str "Auth all fail: " req0 res0))
                     (.redirect res "/login"))))))

(defroute me-handler "get" "/me"
  (let [cred (get-uid-token req res)
        js-root-vars ["userUID" (:uid cred)
                      "shortTermToken" (:token cred)]
        script (get-js-to-def-vars js-root-vars)
        data {:title (str WWW-SITE-TITLE)
              :content (profile-view)
              :scripts [script] }]
    (render req res (render-page data))))
