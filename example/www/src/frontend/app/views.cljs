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

(defsnippet login-view "public/login.html" [:#container]
  [data]
  {})

(defsnippet header "public/page.html" [:#header :> :div]
  [data]
  {[:#greetings] (content (:profile-greetings data))
   [:#user-fullname] (content (get-in data [:user :name]))
   [:#profile-greetings] (set-visibility (:profile-greetings-shown data))
   [:#profile-loading] (set-visibility (:profile-loading-shown data))
   [:#profile-pic] (do-> (set-attr :src (get-in data [:user :photo])) (set-visibility (:profile-pic-shown data)))})

(defsnippet flash "public/page.html" [:#flash :> :div]
  [data]
  {[:#flash-message] (content (:flash-message data))})

(defsnippet profile-view "public/page.html" [:#container]
  [data]
  {[:#header] (content (header data))
   [:#flash]  (do-> (content (flash data)) (set-visibility (:flash-shown data)))})
