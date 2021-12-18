(ns sys-loader.plugin-test
  (:require [sys-loader.plugin :refer [load-plugin-cfg
                                       load-plugins-in-order!
                                       plugin-cfg]]
            [clojure.test :refer [run-tests use-fixtures]]
            [expectations.clojure.test :refer [defexpect expect more-of more]]))

(def ^:dynamic *system*)

(defn with-system [work]
  (let [system (load-plugins-in-order!)]
    (try
      (binding [*system* system]
        (work))
      (finally
        ((-> :sys/db system :server) :stop)
        ((-> :sys/prepl system) :stop)))))

(use-fixtures :once with-system)

#_{:clj-kondo/ignore [:unresolved-symbol]}
(defexpect creates-intrinsic-plugins
  (expect (more-of state
                   map? state
                   (> (count state) 0)
                   (every? state #{:sys/db
                                   :sys/prepl
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