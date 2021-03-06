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

(defn get-all [model query-key then]
  (.find model (clj->js query-key) then))

(defn get-one [model query-key then]
  (.findOne model (clj->js query-key) then))

(defn update- [model query-key data then]
  (.update model (clj->js query-key) (clj->js data) then))

(defn upsert [model query-key data then]
  (let [data-w-key (conj data query-key)]
    (.update model (clj->js query-key) (clj->js data-w-key) (clj->js {:upsert true})
             (fn [err info]
               (if err
                 (then err nil)
                 (get-one model {:uid (:uid data)} (fn [err record] (then nil record))))))))

(defn query [model query-key]
  (.find model (clj->js query-key)))

(defn get-count [model query-key then]
  (doto
    (query model query-key)
    (.count then)))
