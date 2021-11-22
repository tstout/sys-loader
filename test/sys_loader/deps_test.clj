(ns sys-loader.deps-test
  (:require [sys-loader.deps :refer [build-deps order-deps]]
            [clojure.test :refer [run-tests]]
            [expectations.clojure.test :refer [defexpect expect]]))

(def plugin-def [{:sys/description "example plugin"
                  :sys/name :loader
                  :sys/deps [:service-a]
                  :sys/init 'sys-loader.core-test/init}
                 {:sys/description "example plugin2"
                  :sys/name :service-a
                  :sys/deps []
                  :sys/init 'sys-loader.core-test/init}])

(defexpect building
  (expect
   {:loader [:service-a] :service-a []}
   (build-deps plugin-def)))

(defexpect ordering
  (expect
   [:service-a :loader]
   (-> plugin-def build-deps order-deps)))


(comment
  *e

  (run-tests)
  ;;
  )