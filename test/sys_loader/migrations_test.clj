(ns sys-loader.migrations-test
  (:require [expectations.clojure.test :refer [defexpect expect]]
            [sys-loader.migrations :as migrate]
            [clojure.test :refer [run-tests use-fixtures]]
            [sys-loader.db :refer [mk-datasource]]
            [next.jdbc.sql :as sql]
            [next.jdbc.datafy]
            [clojure.datafy :as d]))

(def ^:dynamic *pool* nil)
(def ^:dynamic *con* nil)
(def ^:dynamic *migrate-fn* nil)

(defn pool-setup [work]
  (let [pool (mk-datasource :memory)
        migrate-fn (migrate/init {:sys/db {:data-source pool}})]
    (try
      (binding [*pool* pool *migrate-fn* migrate-fn]
        (work))
      (finally
        (.dispose pool)))))

(defn connection-setup [work]
  (with-open [con (.getConnection *pool*)]
    (binding [*con* con]
      (work))))

(use-fixtures :once pool-setup)
(use-fixtures :each connection-setup)

(defn example-ddl [run-ddl]
  (run-ddl "users"))

(defexpect migration-fn-var-is-persisted
  (expect (fn? *migrate-fn*) true)
  (*migrate-fn* #'example-ddl)
  (expect '("sys-loader.migrations-test/example-ddl")
          (->> (sql/query *con* ["select name from sys_loader.migrations"])
               (map :MIGRATIONS/NAME))))


(comment
  #_(run-tests 'sys-loader.migrations-test)
  *e
  (run-tests)


  (def ds (mk-datasource :memory))

  (def tables
    (with-open [conn (.getConnection ds)]
      (sql/query conn ["select * from information_schema.tables"])))


  tables

  (def db-meta
    (let [conn (.getConnection ds)]
      (.getMetaData conn)))

  (.getCatlog db-meta)

  (bean db-meta)

  (= [1 2] '(1 2))
  
  (type db-meta)
  (d/datafy db-meta)

  (:all-tables (d/datafy db-meta))
  db-meta
  (sql/query ds ["select name from sys_loader.migrations"])

  ;;
  )