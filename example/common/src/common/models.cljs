(ns common.models 
  (:require-macros
    [mern-utils.macros :refer [node-require]])
  (:require 
    [cljs.nodejs :as nodejs]
    [mern-utils.mongoose :as db]
    [common.models.user-schema :refer [userSchema]]
    ))

; create the model for users and expose it to our app
(def user (db/model "User" userSchema))
