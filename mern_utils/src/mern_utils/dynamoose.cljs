(ns mern_utils.dynamoose
  (:require-macros
    [mern-utils.macros :refer [node-require]])
  (:require
    [cljs.nodejs :as nodejs]))

(node-require dynamoose "dynamoose")

(defn schema [fields]
  (let [fields-w-id (assoc fields :id js/String)
        dynamoose-schema (.-Schema dynamoose)]
    (new dynamoose-schema (clj->js fields-w-id))))

(defn model [model-name schema]
  (.model dynamoose model-name schema))

(defn create [model data then]
  (do (println "create invoked" data)
  (.create model (clj->js data) then)))

(defn get-one [model query then]
  (do (println "get-one invoked" query)
      (println (aget (.ddb dynamoose) "endpoint" "host"))
  (.get model (clj->js query) then)))

(defn get-by-id [model id then]
  (do (println "get-by-id " id)
  (get-one model {:id id} then)))

(defn upsert [model query data then]
  (do (println "upsert invoked" data)
  (.update
    model (clj->js query) (clj->js data)
    (fn [err] (if err (create model data then) then)))))
