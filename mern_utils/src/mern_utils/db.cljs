(ns mern-utils.db
  (:require-macros
    [mern-utils.macros :refer [node-require]])
  (:require
    [cljs.nodejs :as nodejs]
    [mern-utils.lib :refer [resolve-cljs]]
    [mern-utils.mongoose]
    [mern-utils.dynamoose]))

(defonce database (atom {}))

(defn connect [db-type endpoint]
  (case db-type
    "mongodb"
    (do 
      (swap! database assoc :ns "mern-utils.mongoose")
      (node-require mongoose "mongoose")
      (.connect mongoose endpoint))
    "dynamodb"
    (do
      (swap! database assoc :ns "mern-utils.dynamoose")
      (node-require dynamoose "dynamoose")
      (if (not (empty? endpoint))
        (.local dynamoose endpoint)))
    (throw (js/Error. "[Error] Database type" db-type "not supported.")))
  (println "Connected to " db-type))

(defn schema [fields]
  (resolve-cljs (str (:ns @database) "/schema") fields))

(defn model [model-name schema]
  (resolve-cljs (str (:ns @database) "/model") model-name schema))

(defn create [model data then]
  (resolve-cljs (str (:ns @database) "/create") model data then))

(defn get-one [model query then]
  (resolve-cljs (str (:ns @database) "/get-one") model query then))

(defn get-by-id [model id then]
  (resolve-cljs (str (:ns @database) "/get-by-id") model id then))

(defn update- [model query data then]
  (resolve-cljs (str (:ns @database) "/update-") model query data then))

(defn upsert [model query data then]
  (resolve-cljs (str (:ns @database) "/upsert") model query data then))
