(ns common.models 
  (:require-macros
    [mern-utils.macros :refer [node-require]])
  (:require 
    [cljs.nodejs :as nodejs]
    [common.models.user-schema :refer [userSchema]]))

(node-require mongoose "mongoose")

; create the model for users and expose it to our app
(def user (.model mongoose "User" userSchema))
