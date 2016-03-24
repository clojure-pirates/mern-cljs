(ns mern-utils.amqp
  (:require-macros
    [mern-utils.macros :refer [node-require]])
  (:require 
    [clojure.string :as string]
    [cognitect.transit :as transit]
    [cljs.nodejs :as nodejs]))

(node-require amqp "amqplib")
(node-require node-when "when")

(defn serialize [data]
  (let [w (transit/writer :json-verbose)]
    (transit/write w (clj->js data))))

(defn deserialize [string]
  (let [r (transit/reader :json)
        input (string/replace (string/replace string  #"\"([^\"]*)\":" #"\"~:$1\":") #"[/\\]" "")]
    (transit/read r input)))

(defn default-msg-handler [msg]
  (println (str " [x] Received " msg)))

(defonce amqp-state (atom {:connection nil :channel nil :default-queue "default_queue" :message-handler default-msg-handler}))

(defn send-to-queue [channel queue msg]
  (.sendToQueue channel queue (js/Buffer. msg) (clj->js {:deliveryMode true}))
  (println " [x] Sent" msg))
;        (.close ch))))))

(defn queue-task [channel queue task]
  (send-to-queue channel queue (serialize task)))

(defn assert-queue [channel queue is-durable]
  (.assertQueue channel (:default-queue @amqp-state) (clj->js {:durable is-durable})))

(defn on-channel-created [channel]
  (swap! amqp-state assoc :channel channel)
  (doto
    (assert-queue channel (:default-queue @amqp-state) true)
    (.then (fn [] (println "Created channel")))))

(defn on-connect [connection]
  (swap! amqp-state assoc :connection connection)
  (doto
    (node-when 
      (-> connection
          (.createChannel)
          (.then on-channel-created)))))
;    (.ensure (fn [] (.close conn)))))

(defn create-amqp-conn [endpoint]
  (-> amqp
      (.connect endpoint)
      (.then on-connect)
      (.then nil println)))

(defn consume-message []
  (let [channel (:channel @amqp-state)]
    (do (.consume channel
                  (:default-queue @amqp-state)
                  (fn [msg] 
                    (let [body (.toString (.-content msg))]
                      ((:message-handler @amqp-state) body)
                      (.ack channel msg)))
                  (clj->js {:noAck false}))
        (println " [*] Waiting for messages. To exit press CTRL+C"))))

(defn worker-on-channel-created [channel]
  (swap! amqp-state assoc :channel channel)
  (doto (assert-queue channel (:default-queue @amqp-state) (clj->js {:durable true}))
        (.then (fn [] (.prefetch channel 1)))
        (.then consume-message)))

(defn worker-on-connect [connection]
  (swap! amqp-state assoc :connection connection)
  (.once nodejs/process "SIGINT" (fn [] (.close connection)))
  (-> connection
      (.createChannel)
      (.then worker-on-channel-created)))

(defn start-worker [endpoint handler success]
  (swap! amqp-state assoc :message-handler handler)
  (-> amqp
      (.connect endpoint)
      (.then worker-on-connect)
      (.then nil println)))
