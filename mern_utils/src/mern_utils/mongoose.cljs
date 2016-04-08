(ns mern_utils.mongoose
  (:require-macros
    [mern-utils.macros :refer [node-require]])
  (:require
    [cljs.nodejs :as nodejs]))

(node-require mongoose "mongoose")

(defn schema [fields]
  (.Schema mongoose (clj->js fields)))

(defn model [model-name schema]
  (.model mongoose model-name schema))

(defn create [model]
  (new model))

(defn get-by-id [model id then]
  (.findById model id then))

(defn get-one [model query then]
  (.findOne model (clj->js query) then))
