(ns mern-utils
      (:require [doo.runner :refer-macros [doo-tests]]
                [mern-utils.lib-test]))

(doo-tests 'mern-utils.lib-test)
