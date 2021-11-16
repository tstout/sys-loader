(ns sys-loader.migrations-test
  (:require [expectations.clojure.test :refer [defexpect expect]]
            [sys-loader.migrations :as migrate]
            [clojure.test :refer [run-tests use-fixtures]]
            [sys-loader.db :refer [mk-datasource]]
            [next.jdbc.sql :as sql]))

(def ^:dynamic *pool*)
(def ^:dynamic *con*)
(def ^:dynamic *migrate-fn*)

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

(defn tables []
  (->> ["select * from information_schema.tables"]
       (sql/query *con*)
       (map #(select-keys % [:TABLES/TABLE_NAME :TABLES/TABLE_SCHEMA]))))

(defn table [t-schema t-name]
  (->>
   (tables)
   (filter #(= (:TABLES/TABLE_NAME %) t-name))
   (filter #(= (:TABLES/TABLE_SCHEMA %) t-schema))))

(defexpect tables-created-and-recorded
  (expect (fn? *migrate-fn*) true)
  (*migrate-fn* #'example-ddl)
  (expect '("sys-loader.migrations-test/example-ddl")
          (->> (sql/query *con* ["select name from sys_loader.migrations"])
               (map :MIGRATIONS/NAME)))
  (expect not-empty (table "SYS_LOADER_TEST" "USERS")))


(comment
  #_(run-tests 'sys-loader.migrations-test)
  *e
  (run-tests)

  (def ds (mk-datasource :memory))


  (def tables
    (with-open [conn (.getConnection ds)]
      (sql/query conn ["select * from information_schema.tables"])))

  tables

  (-> tables first keys)

  (map #(select-keys % [:TABLES/TABLE_NAME :TABLES/TABLE_SCHEMA]) tables)

  ;;
  )