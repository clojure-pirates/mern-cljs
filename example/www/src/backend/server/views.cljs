(ns server.views
  (:require
    [reagent.core :as reagent]
    [kioo.reagent :refer [html-content content add-class append after set-attr do->
                         substitute listen unwrap]]
    [kioo.core :refer [handle-wrapper]])
  (:require-macros
    [kioo.reagent :refer [defsnippet]]))

; Inject each view into content tag in template.html

(defsnippet home-view "public/home.html" [:#container]
  [] {})

(defsnippet login-view "public/login.html" [:#container]
  [] {})

(defsnippet profile-view "public/page.html" [:#container]
  [] {})
