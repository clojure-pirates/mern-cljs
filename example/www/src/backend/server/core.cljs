(ns server.core
  (:require-macros
    [mern-utils.macros :refer [node-require, node-config]])
  (:require
    [polyfill.compat]
    [cljs.nodejs :as nodejs]
    [mern-utils.db :as db]
    [mern-utils.backend-lib :refer [local-ip log]]
    [mern-utils.express :refer [route]]
    [mern-utils.passport.strategy :refer [config-passport]]
    [common.config :refer [LOGGER COOKIE-SECRET DATABASE DB-ENDPOINT AWS-CONFIG
                           WWW-DOMAIN WWW-PORT config-auth cors-options]]
    [common.models :refer [user-model api-token-model facebook-account-model google-account-model]]
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
(node-require bunyan-request "bunyan-request")

(defn server [success]
  (doto (express)
;    (.use (bunyan-request (clj->js {:headerName "X-Request-Id"
;                                       :propertyName "reqId"
;                                       :logName "req_id"
;                                       :obscureHeaders []
;                                       :logger LOGGER})))
    (.use (cors))
    (.use (.static express "resources/public"))
    (.use (morgan "dev"))
    (.use (cookie-parser COOKIE-SECRET))
    (.use (body-parser))
    (.use (express-session (clj->js {:secret COOKIE-SECRET
                                     :resave false
                                     :saveUninitialized true
                                     :cookie {:maxAge (* 1000 60 60)}})))
    (.use (.initialize passport))
    (.use (.session passport))
    (.use (connect-flash))
    (route @route-table cors-options)
    (.listen WWW-PORT WWW-DOMAIN success)
    )
)

(defn -main [& mess]
  (db/connect DATABASE DB-ENDPOINT AWS-CONFIG)
  (config-passport passport config-auth (user-model) (api-token-model) (facebook-account-model) (google-account-model))
  (server #(log LOGGER :info (str "Server running at http://" local-ip ":" WWW-PORT "/"))))

(set! *main-cli-fn* -main)
