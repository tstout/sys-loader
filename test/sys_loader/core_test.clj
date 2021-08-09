(ns sys-loader.core-test
  (:require [clojure.test :refer [use-fixtures]]
            [expectations.clojure.test :refer [defexpect
                                               expect expecting]]))

(defn setup [f] (f))

(use-fixtures :once setup)

(defexpect fix-me-I-fail (expect 1 0))

(comment
  "see https://github.com/clojure-expectations/clojure-test for examples")
