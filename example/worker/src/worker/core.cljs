(ns worker.core
  (:require-macros
    [mern-utils.macros :refer [node-require, node-config]])
  (:require
    [polyfill.compat]
    [cljs.nodejs :as nodejs]
    [clojure.string :as str]
    [mern-utils.amqp :refer [start-worker]]
    [mern-utils.lib :refer [local-ip deserialize resolve-cljs]]
    [common.config :refer [RABBITMQ-DOMAIN RABBITMQ-PORT]]))

(enable-console-print!)

(node-require mongoose "mongoose")
(node-require amqp "amqplib")

(def amqp-endpoint (str "amqp://" RABBITMQ-DOMAIN ":" RABBITMQ-PORT))

(defn ^:export log [data]
  (println "[info] Received message:" (:message data)))
 
(defn task-handler [string]
  (let [task (deserialize string)]
    (resolve-cljs (str "worker.core/" (:fn task)) (:data task))))

(defn -main [& mess]
  (start-worker amqp-endpoint task-handler #(println (str "Worker running at http://" local-ip))))

(set! *main-cli-fn* -main)
