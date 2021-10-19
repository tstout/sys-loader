(ns sys-loader.migrations
  (:require [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            [clojure.java.io :as io]
            [sys-loader.db :refer [mk-datasource]])
  (:import [java.sql Timestamp]))

(defn load-sql
  "Load a resource file representing a sql string. The resource file is expected 
   to have a path of sql/migrations and end with .sql"
  [res]
  (-> (str "sql/migrations/" res ".sql")
      io/resource
      slurp))

(defn run-ddl
  "Execute DDL loaded from a resource file."
  [conn res]
  (->>
   res
   load-sql
   vector
   (jdbc/execute-one! conn)))

(defn var-ns [v]
  {:pre [var?]}
  (let [m (meta v)]
    (str (-> :ns m str) "/" (:name m))))

(defn run-and-record [conn migration]
  (migration (partial run-ddl conn))
  (sql/insert! conn :sys_loader.migrations {:name (var-ns migration)
                                            :created_at (Timestamp. (System/currentTimeMillis))}))

(defn migrate [conn & migrations]
  (run-ddl conn "intrinsic")
  (jdbc/with-transaction [db-conn conn]
    (let [already-run? (->> (sql/query db-conn ["select name from sys_loader.migrations"])
                            (map :SYS_LOADER.MIGRATIONS/NAME)
                            set)]
      (doseq [m migrations
              :when (not (already-run? (var-ns m)))]
        (run-and-record db-conn m)))))

(defn init [state]
  (let [db (-> :sys/db state :data-source)]
    (partial migrate db)))

(comment
  *e
  (def migrator (init {:sys/db {:data-source (mk-datasource)}}))

  (migrator :run-ddl "logging")

  (defn t-migration [run-ddl]
    (run-ddl "logging"))

  (migrator :migrate #'t-migration)

  (load-sql "intrinsic")

  (def ds (mk-datasource))

  (run-ddl ds "intrinsic")

  (-> (sql/query ds ["select NAME from migrations"]) first :MIGRATIONS/NAME)

  (sql/query ds ["select name from migrations"])

  (set
   (map :MIGRATIONS/NAME (sql/query ds ["select name from migrations"])))

  (namespace :LOG/MSG)

  (-> "hello" (doto tap>) seq count)

  ;;
  )


