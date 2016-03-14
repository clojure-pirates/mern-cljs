(ns common.models.user-schema
  (:require-macros
    [cljs.core :refer [this-as]]
    [mern-utils.macros :refer [node-require]])
  (:require
    [cljs.nodejs :as nodejs]))

(node-require mongoose "mongoose")

; define the schema for our user model
(def userSchema
  (.Schema
    mongoose
    (clj->js
      {:uid js/String
       :api {:token js/String :tokenExpiresAt js/String}
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
                 :username js/String}})))
