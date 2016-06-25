(ns server.views
  (:require
    [reagent.core :as reagent]
    [kioo.reagent :refer [html-content content add-class remove-class append
                          after set-attr do-> substitute listen unwrap]]
    [kioo.core :refer [handle-wrapper]]
    [cemerick.url :refer [url-encode]]
    )
  (:require-macros
    [kioo.reagent :refer [defsnippet]]))

(defn set-visibility [is-visible]
  (do-> (remove-class "show" "hide")
        (add-class (if is-visible "show" "hide"))))

; Inject each view into content tag in template.html

(defsnippet home-view "public/home.html" [:#container]
  [] {})

(defsnippet login-view "public/login.html" [:#container]
  [] {})

(defsnippet profile-view "public/page.html" [:#container]
  [] {})
