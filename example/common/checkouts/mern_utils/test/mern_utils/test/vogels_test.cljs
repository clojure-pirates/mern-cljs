(ns mern-utils.test.vogels-test
  (:require-macros [cljs.test :refer [is deftest]]
                   [mern-utils.macros :refer [node-require]])
  (:require [mern-utils.vogels :as vogels]))

(node-require joi "joi")

(deftest test-to-joi
  (is (= (.number joi) (vogels/to-joi js/Number)))
  (is (= (.string joi) (vogels/to-joi js/String))))

(deftest test-vogels-schema
  (is (= {:hashKey "email" :schema {:email (.string joi) :id (.number joi)}}
         (vogels/vogels-schema {:email js/String :id js/Number}))))
