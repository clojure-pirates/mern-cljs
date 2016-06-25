(ns app.atom
 (:require
   [reagent.core :as reagent :refer [atom]]))

(defonce app-state
  (reagent/atom
    {:url-params {}
     :flash-shown false
     :flash-message ""
     :menu-logout-shown false
     :profile-greetings-shown false
     :profile-loading-shown true
     :profile-login-button-shown false
     :profile-pic-shown false
     :profile-greetings ""
     :user {:name "" :photo ""}
     :content-shown false
     }))
