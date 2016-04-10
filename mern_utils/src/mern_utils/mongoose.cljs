(ns mern-utils.mongoose
  (:require-macros
    [mern-utils.macros :refer [node-require]])
  (:require
    [cljs.nodejs :as nodejs]))

(node-require mongoose "mongoose")

(defn schema [fields]
  (.Schema mongoose (clj->js fields)))

(defn in?
  "true if coll contains elm"
  [coll elm]
  (some #(= elm %) coll))

(defn model [model-name schema]
  (let [model-names (.modelNames mongoose)]
    (if (in? model-names model-name)
      (.model mongoose model-name)
      (.model mongoose model-name schema))))

(defn create [model data then]
  (.create model (clj->js data) then))

(defn get-one [model query then]
  (.findOne model (clj->js query) then))

(defn get-by-id [model id then]
  (get-one model {:id id} then))

(defn update- [model query data then]
  (.update model (clj->js query) (clj->js data) then))

(defn upsert [model query data then]
  (.update model (clj->js query) (clj->js data) (clj->js {:upsert true})
           (fn [err info]
             (if err
               (then err nil)
               (get-one model {:uid (:uid data)} (fn [err record] (then nil record)))))))
