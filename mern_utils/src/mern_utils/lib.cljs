(ns mern-utils.lib
  (:require-macros
    [mern-utils.macros :refer [node-require]])
  (:require
    [clojure.string :as str]
    [cognitect.transit :as transit]
    [clojure.walk :refer [keywordize-keys]]
    [cemerick.url :refer [url url-encode]]
    [cljs.nodejs :as nodejs]))

; http://stackoverflow.com/questions/10062967/clojures-equivalent-to-pythons-encodehex-and-decodehex
(defn str->hex
  "Encode numbers into hex string"
  [s]
  (apply str (map #(.toString (int %) 16) s)))

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
        input (str/replace (str/replace string  #"\"([^\"]*)\":" #"\"~:$1\":") #"[/\\]" "")]
    (transit/read r input)))

(defn get-js-to-def-vars
  "Take a list of key value string pairs and produce a JavaScript"
  [key-values]
  (let [pairs (into [] (partition 2 key-values))
        raw (apply str (map #(str (first %) "='" (second %) "',") pairs))]
    (str "var " (subs raw 0 (dec (count raw))) ";")))

(defn set-next-url-from-param
  "Convenience funciton for backend handlers to set next url after auth in session"
  [req fallback]
  (let [query (keywordize-keys (:query (url (.-originalUrl req))))]
    (set! (.. req -session -nextUrl) (str "/" (or (:next query) fallback)))))

(defn get-uid-token-from-request
  "Convenience function for backend handlers to get user uid and token for auth"
  [req]
  {:uid (if (.-user req) (.. req -user -uid) "")
   :token (if (.-user req) (.. req -user -api -token) "")})

(node-require os "os")
(defonce local-ip
  ; from http://stackoverflow.com/questions/3653065/get-local-ip-address-in-node-js
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
