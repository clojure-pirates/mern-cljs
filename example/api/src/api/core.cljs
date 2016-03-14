(ns api.core
  (:require-macros
    [mern-utils.macros :refer [node-require, node-config]])
  (:require
    [polyfill.compat]
    [cljs.nodejs :as nodejs]
    [mern-utils.lib :refer [local-ip]]
    [mern-utils.express :refer [route]]
    [mern-utils.passport.strategy :refer [config-passport]]
    [common.config :refer [MONGODB-DOMAIN MONGODB-PORT MONGODB-DBNAME API-DOMAIN API-PORT WWW-DOMAIN WWW-PORT config-auth cors-options]]
    [common.models :refer [user]]
    [api.handlers :refer [route-table
                          homepage-handler
                          me-handler
                          login-handler]]))

(enable-console-print!)

(node-require express "express")
(node-require express-session "express-session")
(node-require mongoose "mongoose")
(node-require passport "passport")
(node-require connect-flash "connect-flash")
(node-require morgan "morgan")
(node-require body-parser "body-parser")
(node-require cookie-parser "cookie-parser")

(def mongodb-endpoint (str "mongodb://" MONGODB-DOMAIN ":" MONGODB-PORT "/" MONGODB-DBNAME))

(defn server [success]
  (doto (express)
    (.use (.static express "resources/public"))
    (.use (morgan "dev"))
    (.use (cookie-parser))
    (.use (body-parser))
    (.use (express-session (clj->js {:secret "very secret" :cookie {:maxAge (* 1000 60 60)}})))
    (.use (-> passport (.initialize)))
    (.use (-> passport (.session)))
    (.use (connect-flash))
    (route @route-table cors-options)
    (.listen API-PORT API-DOMAIN success)
    )
)

(defn -main [& mess]
  (-> mongoose (.connect mongodb-endpoint))
  (config-passport passport config-auth user)
  (server #(println (str "Server running at http://" local-ip ":" API-PORT "/"))))

(set! *main-cli-fn* -main)
