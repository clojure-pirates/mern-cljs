(ns mern-utils.test.runner
      (:require [doo.runner :refer-macros [doo-tests]]
                [mern-utils.test.lib-test]
                [mern-utils.test.vogels-test]))

(doo-tests 'mern-utils.test.lib-test)
(doo-tests 'mern-utils.test.vogels-test)
