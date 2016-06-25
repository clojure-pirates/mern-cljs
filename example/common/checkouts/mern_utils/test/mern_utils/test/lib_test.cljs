(ns mern-utils.test.lib-test
  (:require-macros [cljs.test :refer [is deftest]])
  (:require [mern-utils.lib :as lib]))

(deftest test-serialize-round-trip-with-quotation
  (is (= {:test "\"hello\""} (lib/deserialize (lib/serialize {:test "\"hello\""})))))
