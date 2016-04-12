(ns mern-utils.dynamoose
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
  (.create model (clj->js data) then))

(defn get-one [model query then]
  (.get model (clj->js query) then))

(defn get-by-id [model id then]
  (get-one model {:id id} then))

(defn update- [model query data then]
  (.update model (clj->js query) (clj->js data) then))

(defn upsert [model query data then]
  (update- model query data
    (fn [err]
      (if err
        (let [data-w-key (conj data query)]
          (create model data-w-key then))
        (then)))))
