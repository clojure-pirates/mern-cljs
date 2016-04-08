(ns common.models.user-schema
  (:require-macros
    [mern-utils.macros :refer [node-require]])
  (:require
    [cljs.nodejs :as nodejs]
    [mern-utils.mongoose :as db]))

; define the schema for our user model
(def userSchema
  (db/schema
    {:uid js/String
     :api {:token js/String :tokenCreatedAt js/Number}
     :email js/String
     :name js/String
     :photo js/String
     :facebook {:id js/String
                :token js/String
                :email js/String
                :name js/String
                :photo js/String}
     :google   {:id js/String
                :token js/String
                :email js/String
                :name js/String
                :photo js/String}
     :twitter {:id js/String
               :token js/String
               :displayName js/String
               :username js/String}}))
