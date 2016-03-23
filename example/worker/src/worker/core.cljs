(ns worker.core
  (:require-macros
    [mern-utils.macros :refer [node-require, node-config]])
  (:require
    [polyfill.compat]
    [cljs.nodejs :as nodejs]
    [mern-utils.amqp :refer [start-worker]]
    [mern-utils.lib :refer [local-ip]]
    [common.config :refer [RABBITMQ-DOMAIN RABBITMQ-PORT]]))

(enable-console-print!)

(node-require mongoose "mongoose")
(node-require amqp "amqplib")

(def amqp-endpoint (str "amqp://" RABBITMQ-DOMAIN ":" RABBITMQ-PORT))

(defn message-handler [msg]
  (println " [x] Received message" msg))

(defn -main [& mess]
;  (-> mongoose (.connect mongodb-endpoint))
  (start-worker amqp-endpoint message-handler #(println (str "Worker running at http://" local-ip))))

(set! *main-cli-fn* -main)
