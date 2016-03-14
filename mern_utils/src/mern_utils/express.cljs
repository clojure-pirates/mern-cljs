(ns mern-utils.express
  (:require-macros
    [cljs.core.async.macros :refer [go]]
    [mern-utils.macros :refer [node-require]])
  (:require
    [cljs.core.async :as async :refer [put!]]
    [cljs.nodejs :as nodejs]  
    [clojure.string :as string]
    [cognitect.transit :as transit]
    [mern-utils.lib :refer [local-ip]]))

(defn write-json-str [x]                                                        
  (let [w (transit/writer :json-verbose)]
    (transit/write w x))) 

(node-require cors "cors")

(defn render [req res page]
  (if (= "https" (aget (.-headers req) "x-forwarded-proto"))
    (.redirect res (str "http://" (.get req "Host") (.-url req)))
    (go
      (.set res "Content-Type" "text/html")
      (.send res (<! page)))))

(defn route [ex route-table cors-options]
  (.options ex "*" (cors cors-options))
  (println ; I don't know why it requires println to make map run :(
    (map
      (fn [r]
        (let [m (:method r)]
          (println "Registering: " m " " (:endpoint r))
          (case m
            "get" (.get ex (:endpoint r) (cors cors-options) (:handler r))
            "post" (.post ex (:endpoint r)  (cors cors-options) (:handler r))
            "put" (.put ex (:endpoint r)  (cors cors-options) (:handler r))
            "delete" (.delete ex (:endpoint r)  cors (cors-options) (:handler r))
            (println "    [Error] Not a METHOD: " m))))
      route-table)
    )
  ex)
