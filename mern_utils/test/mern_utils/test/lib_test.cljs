(ns mern-utils.test.lib-test
  (:require-macros [cljs.test :refer [is deftest]])
  (:require [mern-utils.lib :as lib]))

(deftest test-clean-url
  (is (= "http://www.example.com?35%25" (lib/clean-url "http://www.example.com?35%")))
  (is (= "http://www.example.com?35%25a" (lib/clean-url "http://www.example.com?35%a"))))

(deftest test-serialize-round-trip-with-quotation
  (is (= {:test "\"hello\""} (lib/deserialize (lib/serialize {:test "\"hello\""})))))
