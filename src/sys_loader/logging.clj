(ns sys-loader.logging
  (:require [taoensso.timbre :as log]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql])
  (:import [java.sql Timestamp]
           [java.util Date]))

(defn log-message [db data]
  (let [{:keys [instant level ?ns-str msg_]} data
        entry
        {:instant   (Timestamp. (.getTime ^Date instant))
         :level     (name level)
         :namespace (str ?ns-str)
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
  (log/info "DB Logging Initialized"))


(comment
  *e
  (require '[sys-loader.db :refer [mk-datasource]])

  (def ds (mk-datasource))

  (config-logging ds)

  (log/info "Hello!")

  (time (log/info "Hello3"))

  ;;
  )