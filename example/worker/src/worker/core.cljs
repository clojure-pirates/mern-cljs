(ns worker.core
  (:require-macros
    [mern-utils.macros :refer [node-require, node-config]])
  (:require
    [polyfill.compat]
    [cljs.nodejs :as nodejs]
    [clojure.string :as str]
    [mern-utils.amqp :refer [start-worker]]
    [mern-utils.db :as db]
    [mern-utils.lib :refer [deserialize resolve-cljs]]
    [mern-utils.backend-lib :refer [local-ip log]]
    [common.config :refer [LOGGER
                           DATABASE DB-ENDPOINT AWS-CONFIG
                           RABBITMQ-ENDPOINT
                           ]]
    ))

(enable-console-print!)

(node-require amqp "amqplib")

(defn ^:export log-message [data]
  (log LOGGER :info (str "Received message: " (:message data))))

(defn task-handler [string]
  (log LOGGER :info (str "Received message: " string))
  (let [task (deserialize string)]
    (resolve-cljs (str "worker.core/" (:fn task)) (:data task))))

(defn -main [& mess]
  (db/connect DATABASE DB-ENDPOINT AWS-CONFIG)
  (start-worker RABBITMQ-ENDPOINT task-handler #(log LOGGER :info (str "Worker running at http://" local-ip))))

(set! *main-cli-fn* -main)
