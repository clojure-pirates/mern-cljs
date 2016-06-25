(ns common.models
  (:require-macros
    [mern-utils.macros :refer [node-require]])
  (:require
    [cljs.nodejs :as nodejs]
    [mern-utils.db :as db]
    [common.models.user-schema :refer [user-schema api-token-schema
                                       facebook-account-schema
                                       google-account-schema
                                       twitter-account-schema]]
    ))

(defn user-model [] (db/model "User" (user-schema)))
(defn api-token-model [] (db/model "ApiToken" (api-token-schema)))
(defn facebook-account-model [] (db/model "FacebookAccount" (facebook-account-schema)))
(defn google-account-model [] (db/model "GoogleAccount" (google-account-schema)))
(defn twitter-account-model [] (db/model "TwitterAccount" (twitter-account-schema)))
