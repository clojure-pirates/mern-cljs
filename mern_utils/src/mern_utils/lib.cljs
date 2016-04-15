(ns mern-utils.lib
  (:require-macros
    [mern-utils.macros :refer [node-require]])
  (:require
    [clojure.string :as str]
    [cognitect.transit :as transit]
    [clojure.walk :refer [keywordize-keys]]
    [cemerick.url :refer [url url-encode]]
    [cljs.nodejs :as nodejs]))

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
        input (-> string
                  (str/replace #"\"([^\"]*)\":" #"\"~:$1\":")
                  (str/replace #"/\\" "")
                  (str/replace "\\\":" "\":")
                  (str/replace "/\"" "\"")
                  (str/replace "/'/" "'")
                  (str/replace "/{" "{"))]
    (transit/read r input)))

(defn get-js-to-def-vars
  "Take a list of key value string pairs and produce a JavaScript"
  [key-values]
  (let [pairs (into [] (partition 2 key-values))
        raw (apply str (map #(str (first %) "='" (str/replace (second %) #"'" #"\'") "',") pairs))]
    (str "var " (subs raw 0 (dec (count raw))) ";")))

(defn get-url-params [req]
  (keywordize-keys (:query (url (.-originalUrl req)))))

(defn set-next-url-from-param
  "Convenience funciton for backend handlers to set next url after auth in cookie"
  [req res fallback]
  (let [params (get-url-params req)]
    (println (:next params))
    (.cookie res "nextUrl" (str "/" (or (:next params) fallback)))))

(defn get-uid-token
  "Convenience function for backend handlers to get user uid and token for auth"
  [req res]
  (if (.-user req)
    (let [uid (.. req -user -uid)
          token (.. req -user -api -token)]
      (.cookie res "userUid" uid)
      (.cookie res "apiToken" token)
      {:uid uid :token token})
  (let [uid (.. req -cookies -userUid)
        token (.. req -cookies -apiToken)]
    (if (and uid token)
      {:uid uid :token token}
      {:uid "" :token ""}))))

; from http://stackoverflow.com/questions/3653065/get-local-ip-address-in-node-js
(node-require os "os")
(defonce local-ip
  (let [iface-groups (.networkInterfaces os)
        ifnames (into [] (.keys js/Object iface-groups))]
    (loop [ifnames ifnames]
      (let [ifaces (into [] (aget iface-groups (last ifnames)))
            banned ["192.168.99.1"]
            address
            (loop [ifaces ifaces]
              (let [iface (last ifaces)
                    family   (.-family   iface)
                    internal (.-internal iface)
                    address  (.-address  iface)]
                (if (and (= "IPv4" family) (false? internal) (not-any? #(= address %) banned))
                  address
                  (if (< 1 (count ifaces))
                    (recur (pop ifaces))))))]
        (if address
          address
          (if (< 1 (count ifnames))
            (recur (pop ifnames))))))))
