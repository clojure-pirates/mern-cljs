(ns mern-utils.db
  (:require-macros
    [mern-utils.macros :refer [node-require]])
  (:require
    [cljs.nodejs :as nodejs]))


(defn connect [db-type endpoint]
  (case db-type
    "mongodb"
    (do 
      (node-require mongoose "mongoose")
      (.connect mongoose endpoint))
    "dynamodb"
    (do
      (println "invoking node-require dynamoose...")
      (node-require dynamoose "dynamoose")
      (println "...done")
      (if (not (empty? endpoint))
        (do (.local dynamoose endpoint)
            (println "cljs" (.-endpointURL dynamoose))
            (println "cljs" (aget (.ddb dynamoose) "endpoint" "host"))
            ))) ; Is there a way to verify connection?
    (throw (js/Error. "[Error] Database type" db-type "not supported.")))
  (println "Connected to " db-type))
