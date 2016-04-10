(ns common.models.user-schema
  (:require-macros
    [mern-utils.macros :refer [node-require]])
  (:require
    [cljs.nodejs :as nodejs]
    [mern-utils.db :as db]))

; define the schema for our user model
(defn user-schema []
  (db/schema
    {:uid js/String
     :name js/String
     :email js/String
     :photo js/String}))

(defn api-token-schema []
  (db/schema
    {:userUid js/String
     :token js/String
     :tokenCreatedAt js/Number}))

(defn facebook-account-schema []
  (db/schema
    {:id js/String
     :userUid js/String
     :token js/String
     :email js/String
     :name js/String
     :photo js/String}))

(defn google-account-schema []
  (db/schema
    {:id js/String
     :userUid js/String
     :token js/String
     :email js/String
     :name js/String
     :photo js/String}))

(defn twitter-account-schema []
  (db/schema
    {:id js/String
     :userUid js/String
     :token js/String
     :displayName js/String
     :username js/String}))
