(ns app.views
  (:require
   [kioo.reagent :refer [html-content content append after set-attr do->
                         substitute listen unwrap add-class remove-class]]
   [kioo.core :refer [handle-wrapper]]
   [reagent.core :as reagent :refer [atom]]
   [goog.string :as gstring])
  (:require-macros
   [kioo.reagent :refer [defsnippet deftemplate snippet]]))

(defn set-visibility [is-visible]
  (do-> (remove-class "show" "hide")
        (add-class (if is-visible "show" "hide"))))

(defsnippet login-view "public/login.html" [:#content]
  [data]
  {})

(defsnippet profile-view "public/profile.html" [:#content]
  [data]
  {[:#flash] (do-> (content (:flash data)) (set-visibility (:flash-shown data)))
   [:#profile-loading] (set-visibility (:profile-loading-shown data))
   [:#profile] (set-visibility (:profile-shown data))
   [:#greetings] (content (:profile-greetings data))
   [:#user-fullname] (content (get-in data [:user :name]))
   [:#profile-pic] (set-attr :src (get-in data [:user :photo]))})
