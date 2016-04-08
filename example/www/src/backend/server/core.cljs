(ns server.core
  (:require-macros
    [mern-utils.macros :refer [node-require, node-config]])
  (:require
    [polyfill.compat]
    [cljs.nodejs :as nodejs]
    [mern-utils.db :as db]
    [mern-utils.lib :refer [local-ip]]
    [mern-utils.express :refer [route]]
    [mern-utils.passport.strategy :refer [config-passport]]
    [common.config :refer [DATABASE DB-ENDPOINT WWW-DOMAIN WWW-PORT config-auth cors-options]]
    [common.models :refer [user]]
    [server.handlers :refer [route-table]]))

(enable-console-print!)

(node-require express "express")
(node-require express-session "express-session")
(node-require cors "cors")
(node-require passport "passport")
(node-require connect-flash "connect-flash")
(node-require morgan "morgan")
(node-require body-parser "body-parser")
(node-require cookie-parser "cookie-parser")

(defn server [success]
  (doto (express)
    (.use (cors))
    (.use (.static express "resources/public"))
    (.use (morgan "dev"))
    (.use (cookie-parser))
    (.use (body-parser))
    (.use (express-session (clj->js {:secret "very secret" :cookie {:maxAge (* 1000 60 60)}})))
    (.use (.initialize passport))
    (.use (.session passport))
    (.use (connect-flash))
    (route @route-table cors-options)
    (.listen WWW-PORT WWW-DOMAIN success)
    )
)

(defn -main [& mess]
  (db/connect DATABASE DB-ENDPOINT)
  (config-passport passport config-auth user)
  (server #(println (str "Server running at http://" local-ip ":" WWW-PORT "/"))))

(set! *main-cli-fn* -main)
