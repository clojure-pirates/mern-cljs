(ns mern-utils.frontend-lib
  (:require 
    [goog.dom :as dom]
    [clojure.string :as str]
    [cemerick.url :refer [url]]
    [clojure.walk :refer [keywordize-keys]]
    [ajax.core :refer [GET POST]]))

(defn redirect [url]
  (set! (.-location (dom/getWindow)) url))

(defn get-location []
  (str (.-location (dom/getWindow))))

(defn get-path []
  (:path (url (get-location))))

(defn get-url-params []
  (keywordize-keys (:query (url (get-location)))))

(defn get-elems-by-tag-name [tag]
  (dom/getElementsByTagNameAndClass tag))

(defn get-js-var
  "Get global JS variables"
  [var-name]
  (aget (js* "window") var-name))

(defn post-json [uri data nextfn errorfn]
  (POST uri
    {:params data
     :format :json
     :response-format :json
     :keywords? true
     :with-credentials true
     :handler nextfn
     :error-handler errorfn}))

(defn get-json [uri nextfn errorfn]
  (GET uri
    {:response-format :json
     :keywords? true
     :with-credentials true
     :handler nextfn
     :error-handler errorfn}))
