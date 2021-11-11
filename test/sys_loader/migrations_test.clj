(ns sys-loader.migrations-test
  (:require [expectations.clojure.test :refer [defexpect expect]]
            [sys-loader.migrations :as migrate]
            [clojure.test :refer [run-tests]]
            [sys-loader.db :refer [mk-datasource]]
            [next.jdbc.sql :as sql]
            [next.jdbc.datafy]
            [next.jdbc :as jdbc]
            [clojure.datafy :as d]))


(defn example-ddl [run-ddl]
  (run-ddl "users"))

(defexpect migration-fn-var-is-persisted
  (let [ds (mk-datasource :memory)
        migrate (migrate/init {:sys/db {:data-source ds}})]
    (expect (fn? migrate) true)
    (migrate #'example-ddl)
    (with-open [conn (.getConnection ds)]
      (expect '("sys-loader.migrations-test/example-ddl")
              (->> (sql/query conn ["select name from sys_loader.migrations"])
                   (map :MIGRATIONS/NAME))))))


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

  (type db-meta)
  (d/datafy db-meta)

  (:all-tables (d/datafy db-meta))
  db-meta
  (sql/query ds ["select name from sys_loader.migrations"])

  ;;
  )