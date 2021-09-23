(ns sys-loader.logging
  (:require [taoensso.timbre :as log]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql])
  (:import [java.sql Timestamp]
           [java.util Date]))

(defn log-message [db data]
  (let [{:keys [instant level ?ns-str msg_ ?line]} data
        entry
        {:instant   (Timestamp. (.getTime ^Date instant))
         :level     (name level)
         :namespace (str ?ns-str)
         :line      ?line
         :msg       (str (force msg_))}]
    (with-open [conn (jdbc/get-connection db)]
      (sql/insert! conn :log entry))))

(defn h2-appender [db]
  {:enabled?   true
   :async?     true
   :min-level  nil
   :rate-limit nil
   :output-fn  :inherit
   :fn         (fn [data] (log-message db data))})

(defn config-logging [db]
  (log/set-level! :debug)
  (log/merge-config! {:appenders {:h2 (h2-appender db)}
                      :timestamp-opts {:pattern "yyyy-MM-dd HH:mm:ss.SS"}})
  (log/info "Logging Initialized"))


(defn logging-ddl [run-ddl]
  (run-ddl "logging"))

(defn init [state]
  (let [db (-> :sys/db state :data-source)
        migrate (-> :sys/migrations state)]
    (migrate #'logging-ddl)
    (config-logging db)))

(comment
  *e
  (require '[sys-loader.db :refer [mk-datasource]])

  (def ds (mk-datasource))

  (config-logging ds)

  (log/info "Hello!--")

  (time (log/info "Hello3"))

  ;;
  )