(ns mern-utils
      (:require [doo.runner :refer-macros [doo-tests]]
                [mern-utils.lib-test]
                [mern-utils.vogels-test]))

;(doo-tests 'mern-utils.lib-test)
(doo-tests 'mern-utils.vogels-test)
