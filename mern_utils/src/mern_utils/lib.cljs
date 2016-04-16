(ns mern-utils.lib
  (:require-macros
    [mern-utils.macros :refer [node-require]])
  (:require
    [clojure.string :as str]
    [cognitect.transit :as transit]
    [clojure.walk :refer [keywordize-keys]]
    [cemerick.url :refer [url url-encode]]))

(defn raise [err]
  (throw (js/Error. err)))

; http://stackoverflow.com/questions/10062967/clojures-equivalent-to-pythons-encodehex-and-decodehex
(defn str->hex
  "Encode numbers into hex string"
  [s]
  (apply str (map #(.toString (js/Number (int %)) 16) s)))

(defn ->js [var-name]
  (-> var-name
    (str/replace #"/" ".")
    (str/replace #"-" "_")))

; http://stackoverflow.com/questions/23345663/call-a-clojurescript-function-by-string-name/30892955#30892955
(defn resolve-cljs
  "clj resolve equivalent. Call a cljs function by string name"
  [function-name & args]
  (let [fun (js/eval (->js function-name))]
    (apply fun args)))

(defn serialize [data]
  "Serialize cljs map into json"
  (let [w (transit/writer :json-verbose)]
    (transit/write w (clj->js data))))

(defn deserialize [string]
  "Deserialize json into cljs map"
  (let [r (transit/reader :json)
        input (str/replace string #"\"([^\"]*)\":" "\"~:$1\":")]
    (transit/read r input)))

(defn set-timeout
  "Async execute with delay in miliseconds"
  [then msec]
  (js/setTimeout then msec))
