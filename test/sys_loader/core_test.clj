(ns sys-loader.core-test
  (:require [clojure.test :refer [use-fixtures]]
            [taoensso.timbre :as log]
            [expectations.clojure.test :refer [defexpect
                                               expect expecting]]))

(defn setup [f] (f))

(use-fixtures :once setup)

(defexpect example-test-label (expect 1 1))


(defn init1 []
  (log/info "-----Plugin Init-1---------"))

(defn init2 []
  (log/info "-----Plugin Init-2---------"))

(comment
  "see https://github.com/clojure-expectations/clojure-test for examples")
