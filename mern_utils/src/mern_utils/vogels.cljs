(ns mern-utils.vogels
  (:require-macros
    [mern-utils.macros :refer [node-require]])
  (:require
    [cljs.nodejs :as nodejs]))

(node-require vogels "vogels")
(node-require joi "joi")

(defn schema [fields]
  (let [fields-w-id (assoc fields :id js/String)]
    fields-w-id))

(defn to-joi [object-type]
  (cond
    (= object-type js/Boolean) (.boolean joi)
    (= object-type js/Number) (.number joi)
    (= object-type js/String) (.string joi)))

(defn vogels-schema [schema]
  (let [schema-keys (keys schema)]
    {:hashKey (name (first schema-keys))
     :schema (apply merge (map #(hash-map % (to-joi (% schema))) schema-keys))}))

(defn model [model-name schema]
  (let [new-model (.define vogels model-name (clj->js (vogels-schema schema)))]
    (.createTable new-model (fn [err result] (println "created table" model-name err result)))
    new-model))

(defn attrs [err record then]
  (then err (if record (.-attrs record) nil)))

(defn create [model data then]
  (.create model (clj->js data) (fn [err record] (attrs err record then))))

(defn get-one [model query then]
  (.get model (clj->js query) (fn [err record] (attrs err record then))))

(defn update- [model query data then]
  (let [data-w-key (conj data query)]
    (.update model (clj->js data-w-key) (fn [err record] (attrs err record then)))))

(defn get-by-id [model id then]
  (get-one model {:id id} then))

(defn upsert [model query data then]
  (update- model query data
    (fn [err record]
      (println "upsert" err record)
      (if err
        (let [data-w-key (conj data query)]
          (create model data-w-key then))
        (then err record)))))
