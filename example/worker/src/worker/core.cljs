(ns worker.core
  (:require-macros
    [mern-utils.macros :refer [node-require, node-config]])
  (:require
    [polyfill.compat]
    [cljs.nodejs :as nodejs]
    [clojure.string :as str]
    [mern-utils.amqp :refer [start-worker]]
    [mern-utils.db :as db]
    [mern-utils.backend-lib :refer [local-ip]]
    [mern-utils.lib :refer [deserialize resolve-cljs]]
    [common.config :refer [DATABASE DB-ENDPOINT
                           RABBITMQ-DOMAIN RABBITMQ-PORT]]))

(enable-console-print!)

(node-require amqp "amqplib")

(def amqp-endpoint (str "amqp://" RABBITMQ-DOMAIN ":" RABBITMQ-PORT))

; Note "^:export" is necessary for a function to be called by resolve-cljs
(defn ^:export log [data]
  (println "[info] Received message:" (:message data)))
 
(defn task-handler [string]
  (let [task (deserialize string)]
    (resolve-cljs (str "worker.core/" (:fn task)) (:data task))))

(defn -main [& mess]
  (db/connect DATABASE DB-ENDPOINT)
  (start-worker amqp-endpoint task-handler #(println (str "Worker running at http://" local-ip))))

(set! *main-cli-fn* -main)
