(ns mern-utils.lib
  (:require-macros
    [mern-utils.macros :refer [node-require]])
  (:require
    [clojure.string :as str]
    [cognitect.transit :as transit]
    [cljs.nodejs :as nodejs]))

; http://stackoverflow.com/questions/10062967/clojures-equivalent-to-pythons-encodehex-and-decodehex
(defn str->hex [s]
  (apply str (map #(.toString (int %) 16) s)))

; http://stackoverflow.com/questions/23345663/call-a-clojurescript-function-by-string-name/30892955#30892955
(defn ->js [var-name]
  (-> var-name
    (str/replace #"/" ".")
    (str/replace #"-" "_")))

; http://stackoverflow.com/questions/23345663/call-a-clojurescript-function-by-string-name/30892955#30892955
(defn resolve-cljs [function-name & args]
  (let [fun (js/eval (->js function-name))]
    (apply fun args)))

(defn serialize [data]
  (let [w (transit/writer :json-verbose)]
    (transit/write w (clj->js data))))

(defn deserialize [string]
  (let [r (transit/reader :json)
        input (str/replace (str/replace string  #"\"([^\"]*)\":" #"\"~:$1\":") #"[/\\]" "")]
    (transit/read r input)))

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
