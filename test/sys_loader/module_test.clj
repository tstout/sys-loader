(ns sys-loader.module-test
  (:require [sys-loader.module :refer [load-module-cfg
                                       load-modules-in-order!
                                       module-cfg]]
            [sys-loader.bootstrap :refer [boot]]
            [clojure.test :refer [run-tests use-fixtures]]
            [expectations.clojure.test :refer [defexpect expect more-of more]]))

(def ^:dynamic *system*)

(defn with-system [work]
  (let [system (load-modules-in-order! @boot)]
    (try
      (binding [*system* system]
        (work))
      (finally
        ((-> :sys/db system :server) :stop)
        #_((-> :sys/prepl system) :stop)))))

(use-fixtures :once with-system)

#_{:clj-kondo/ignore [:unresolved-symbol]}
(defexpect creates-intrinsic-modules
  (expect (more-of state
                   map? state
                   (> (count state) 0)
                   (every? state #{:sys/db
                                   ;;:sys/prepl
                                   :sys/migrations
                                   :sys/logging}))
          *system*))


(comment
  *e
  *system*
  (run-tests)

  (map #(for [[k v] %] {k (inc v)}) '({:a 2} {:a 3}))

  ;;
  )