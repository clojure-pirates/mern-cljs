(ns mern-utils.db
  (:require-macros
    [mern-utils.macros :refer [node-require]])
  (:require
    [cljs.nodejs :as nodejs]
    [mern-utils.lib :refer [resolve-cljs]]
    [mern-utils.mongoose]
    [mern-utils.vogels]))

(defonce database (atom {}))

(defn connect [db-type endpoint & aws-config]
  (case db-type
    "mongodb"
    (do 
      (swap! database assoc :ns "mern-utils.mongoose")
      (node-require mongoose "mongoose")
      (.connect mongoose endpoint))
    "dynamodb"
    (do
      (swap! database assoc :ns "mern-utils.vogels")
      (node-require vogels "vogels")
      (if (not (empty? endpoint))
        (do
          (node-require aws "aws-sdk")
          (let [aws-endpoint-obj (.-Endpoint aws)
                aws-endpoint (new aws-endpoint-obj endpoint)
                aws-dynamodb-obj (.-DynamoDB aws)
                dynamodb (new aws-dynamodb-obj (clj->js {:endpoint aws-endpoint}))]
            (.update (.-config aws)
                     (clj->js
                       {:accessKeyId (:accessKeyId aws-config)
                        :secretAccessKey (:secretAccessKey aws-config)
                        :region (:region aws-config)}))
            (.dynamoDriver vogels dynamodb)))))
    (throw (js/Error. "[Error] Database type" db-type "not supported.")))
  (println "Connected to " db-type))

(defn schema [fields]
  (resolve-cljs (str (:ns @database) "/schema") fields))

(defn model [model-name schema]
  (resolve-cljs (str (:ns @database) "/model") model-name schema))

(defn create [model data then]
  (resolve-cljs (str (:ns @database) "/create") model data then))

(defn get-all [model query then]
  (resolve-cljs (str (:ns @database) "/get-all") model query then))

(defn get-one [model query then]
  (resolve-cljs (str (:ns @database) "/get-one") model query then))

(defn get-by-id [model id then]
  (resolve-cljs (str (:ns @database) "/get-by-id") model id then))

(defn get-count [model query then]
  (resolve-cljs (str (:ns @database) "/get-count") model query then))

(defn update- [model query data then]
  (resolve-cljs (str (:ns @database) "/update-") model query data then))

(defn upsert [model query data then]
  (resolve-cljs (str (:ns @database) "/upsert") model query data then))

(defn query [model query]
  (resolve-cljs (str (:ns @database) "/query") model query))
