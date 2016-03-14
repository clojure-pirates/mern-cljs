(ns app.utils
  (:require 
    [goog.dom :as dom]
    [cemerick.url :refer (url url-encode)]
    [clojure.walk :refer [keywordize-keys]]
    [ajax.core :refer [GET POST]]))

(defn redirect [url]
  (set! (.-location (dom/getWindow)) url))

(defn get-location []
   (url (-> (dom/getWindow) .-location)))

(defn get-path []
  (:path (get-location)))

(defn get-url-params []
  (keywordize-keys (:query (get-location))))

(defn get-elems-by-tag-name [tag]
  (dom/getElementsByTagNameAndClass tag))

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
