(ns mern-utils.db
  (:require-macros
    [mern-utils.macros :refer [node-require]])
  (:require
    [cljs.nodejs :as nodejs]))


(defn connect [db-type endpoint]
  (case db-type
    "mongodb"
    (do 
      (println endpoint)
      (node-require mongoose "mongoose")
      (.connect mongoose endpoint))
    "dynamodb"
    (do
      (node-require dynamoose "dynamoose")
      (if (= "local" endpoint)
        (.local dynamoose)))
    (throw (js/Error. "[Error] Database type" db-type "not supported.")))
  (println "Connected to " db-type))
