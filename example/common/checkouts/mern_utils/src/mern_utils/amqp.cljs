(ns mern-utils.amqp
  (:require-macros
    [mern-utils.macros :refer [node-require]])
  (:require
    [cljs.nodejs :as nodejs]
    [mern-utils.backend-lib :refer [log DEFAULT-LOGGER]]
    [mern-utils.lib :refer [serialize]]))

(node-require amqp "amqplib")
(node-require node-when "when")

(defn default-msg-handler [msg]
  (log DEFAULT-LOGGER :info (str " [x] Received " msg)))

(defonce amqp-state (atom {:connection nil :channel nil :default-queue "default_queue" :message-handler default-msg-handler}))

(defn send-to-queue [channel queue msg]
  (.sendToQueue channel queue (js/Buffer. msg) (clj->js {:deliveryMode true}))
  (log DEFAULT-LOGGER :info (str " [x] Sent" msg)))

(defn queue-task [channel queue task]
  (send-to-queue channel queue (serialize task)))

(defn assert-queue [channel queue is-durable]
  (.assertQueue channel (:default-queue @amqp-state) (clj->js {:durable is-durable})))

(defn on-channel-created [channel]
  (swap! amqp-state assoc :channel channel)
  (doto
    (assert-queue channel (:default-queue @amqp-state) true)
    (.then (fn [] (log DEFAULT-LOGGER :info "Created channel")))))

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
        (log DEFAULT-LOGGER :info " [*] Waiting for messages. To exit press CTRL+C"))))

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
