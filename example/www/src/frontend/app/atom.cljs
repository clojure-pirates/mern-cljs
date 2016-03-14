(ns app.atom
 (:require
   [reagent.core :as reagent :refer [atom]]))

(defonce app-state
  (reagent/atom
    {:flash ""
     :flash-shown false
     :profile-loading-shown true
     :profile-shown false
     :profile-greetings "Hello"
     :user {:name "" :photo "image/facebook-profile-picture.jpg"}
     :image-url "/image/loading.gif"
     :url-params {}}))
