(ns sys-loader.plugin-test
  (:require [sys-loader.plugin :refer [load-plugin-cfg
                                       load-plugins-in-order!
                                       plugin-cfg]]
            [clojure.test :refer [run-tests use-fixtures]]
            [expectations.clojure.test :refer [defexpect expect]]))

(def ^:dynamic *system*)

(defn with-system [work]
  (let [system (load-plugins-in-order!)]
    (try
      (binding [*system* system]
        (work))
      (finally
        ((-> :sys/db system :server) :stop)))))

(use-fixtures :once with-system)

#_{:clj-kondo/ignore [:unresolved-symbol]}
(defexpect creates-and-starts-plugins
  (expect (map? *system*))
  #_(expect (more-of {:keys [:sys/db]}
                     map? sys/db)
            *system*))


(comment
  *e
  *system*
  (run-tests)
  ;;
  )