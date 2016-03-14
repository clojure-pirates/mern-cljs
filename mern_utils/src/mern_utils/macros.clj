(ns mern-utils.macros)

(defmacro node-require 
  "Macro to load node module"
  [module module-path]
  `(do
    (defonce ~'node-module-table (atom {}))
    (swap! ~'node-module-table assoc (keyword ~module-path) (cljs.nodejs/require ~module-path))
    (def ~module ((keyword ~module-path) (deref ~'node-module-table)))
  ))

(defmacro node-config
  "Macro to load node config"
  [config config-path]
  `(do
    (defonce ~'node-config-table (atom {}))
    (swap! ~'node-config-table assoc (keyword ~config-path) (cljs.nodejs/require ~config-path))
    (def ~config ((keyword ~config-path) (deref ~'node-config-table)))
  ))

(defmacro defroute
  "Macro define a route"
  [fnname method endpoint handler-func]
  `(do
    (defn ~(symbol (str fnname)) [~'req ~'res]  (~@handler-func))
    (swap! ~'route-table concat [{:method ~method :endpoint ~endpoint :handler ~fnname}])
  ))
