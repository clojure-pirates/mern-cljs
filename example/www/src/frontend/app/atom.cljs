(ns app.atom
 (:require
   [reagent.core :as reagent :refer [atom]]))

(defonce app-state
  (reagent/atom
    {:url-params {}
     :flash-shown false
     :flash-message ""
     :profile-greetings-shown false
     :profile-loading-shown true
     :profile-pic-shown false
     :profile-greetings "Hello"
     :user {:name "" :photo "/image/facebook-profile-picture.jpg"}
     :content-shown false
     }))
