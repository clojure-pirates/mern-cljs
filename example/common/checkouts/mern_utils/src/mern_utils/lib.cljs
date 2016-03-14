(ns mern-utils.lib
  (:require-macros
    [mern-utils.macros :refer [node-require]])
  (:require
    [cljs.nodejs :as nodejs]))

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
