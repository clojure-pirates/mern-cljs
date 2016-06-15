(ns mern-utils.vogels
  (:require-macros
    [mern-utils.macros :refer [node-require]])
  (:require
    [cljs.nodejs :as nodejs]
    [mern-utils.backend-lib :refer [log DEFAULT-LOGGER]]))

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

(defn get-rangekey [schema]
  (let [schema-keys (keys schema)]
    (loop [n 0]
      (if (<= (count schema-keys) n) nil
        (let [current-key (nth schema-keys n)]
          (if (and (map? (current-key schema)) (:rangeKey (current-key schema)))
            (name current-key)
            (recur (inc n))))))))

(defn get-secondary-index-keys [schema]
  (let [schema-keys (keys schema)]
    (reduce (fn [coll x] (if (nil? x) coll (conj coll x)))
            []
            (map #(if (map? (% schema)) (if (:index (% schema)) % nil) nil) schema-keys))))

(defn get-indexes [schema hashkey]
  (let [secondary-index-keys (get-secondary-index-keys schema)]
    (map #(hash-map :hashKey hashkey
                    :rangeKey (name %)
                    :name (str (name %) "_index")
                    :type "global")
         secondary-index-keys)))

(defn vogels-schema [schema]
  (let [schema-keys (keys schema)
        hashkey (name (first schema-keys))
        rangekey (get-rangekey schema)
        indexes (get-indexes schema hashkey)
        vschema {:hashKey hashkey
                 :schema (apply merge
                                (map #(let [type- (if (map? (% schema)) (:type (% schema)) (% schema))]
                                        (hash-map % (to-joi type-))) schema-keys))
                 :indexes indexes}]
    (if (nil? rangekey)
      vschema
      (assoc vschema :rangeKey rangekey))))

(defn model [model-name schema]
  (let [new-model (.define vogels model-name (clj->js (vogels-schema schema)))]
    (.createTable new-model
                  (fn [err result]
                    (log DEFAULT-LOGGER :info (str "created table" model-name err result))))
    new-model))

(defn attrs [err record then]
  (then err (if record (.-attrs record) nil)))

(defn create [model data then]
  (.create model (clj->js data) (fn [err record] (attrs err record then))))

(defn get-
  "Only the first of the query key is used and it is assumed to be the hashkey"
  [model query-key then]
  (let [first-key (first (keys query-key))
        hashkey (first-key query-key)]
    (.get model (clj->js query-key) (fn [err record] (attrs err record then)))))

(defn get-one [model query-key then]
  (get- model query-key then))

(defn update- [model query-key data then]
  (let [data-w-key (conj data query-key)]
    (.update model (clj->js data-w-key) (fn [err record] (attrs err record then)))))

(defn upsert [model query-key data then]
  (update- model query-key data
    (fn [err record]
      (if err
        (let [data-w-key (conj data query-key)]
          (create model data-w-key then))
        (then err record)))))

(defn query
  [model query-key]
  (let [hashkey (first (keys query-key))]
    (-> model
        (.query (hashkey query-key)))))

(defn exec [query then]
  (.exec query then))

(defn where [query query-key]
  (let [rangekey (first (keys query-key))]
    (-> model
        (.query (rangekey query-key)))))

(defn using-index [query index]
  (.usingIndex (str (name index) "_index")))

(defn limit [query n]
  (.limit query n))

(defn select [query index]
  (.select query index))

(defn get-count [model query-key then]
  (doto (query model query-key)
        (select "COUNT")
        (exec (fn[err result] (if err (then err nil) (then err (.-Count result)))))))
