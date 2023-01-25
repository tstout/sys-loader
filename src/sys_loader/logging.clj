(ns sys-loader.logging
  (:require [clojure.tools.logging :as log]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            [clojure.reflect :as cr]
            [clojure.pprint :as pp]
            [clojure.string :refer [split]])
  (:import [java.sql Timestamp]
           [java.util Date]
           [org.apache.logging.log4j.message Message]
           [org.apache.logging.log4j.core.appender AbstractAppender]
           [org.apache.logging.log4j.core LogEvent]
           [org.apache.logging.log4j.core.config Configurator]
           [org.apache.logging.log4j.core.config.builder.api ConfigurationBuilder ConfigurationBuilderFactory]))

(defn mk-appender []
  (proxy [AbstractAppender] ["H2Appender" nil nil]
    (append [^LogEvent evt]
      (prn (format "from appender>>>: %s" (.getFormattedMessage evt))))))



(defn log-message [db data]
  (let [{:keys [instant level ?ns-str msg_ ?line ?file]} data
        entry
        {:instant   (Timestamp. (.getTime ^Date instant))
         :level     (name level)
         :namespace ?ns-str
         :line      ?line
         :file      (-> ?file (split #"/") last)
         :msg       (force msg_)}]
    (with-open [conn (jdbc/get-connection db)]
      (sql/insert! conn :sys_loader.log entry))))

(defn h2-appender [db]
  {:enabled?   true
   :async?     true
   :min-level  nil
   :rate-limit nil
   :output-fn  :inherit
   :fn         (fn [data] (log-message db data))})

(defn config-logging [db]
  ;;(log/set-level! :debug)
  ;;(log/merge-config! {:appenders {:h2 (h2-appender db)}
                      ;;#_:timestamp-opts #_{:pattern "yyyy-MM-dd HH:mm:ss.SS"}})

  ;; (let [builder (ConfigurationBuilderFactory/newConfigurationBuilder)]
  ;;   (.add builder (mk-appender))
  ;;   (Configurator/initialize (.build builder)))

  ;;Configurator.initialize (builder.build ());

  (log/info "Logging Initialized"))


(defn logging-ddl [run-ddl]
  (run-ddl "logging"))

(defn init [state]
  ;; TODO nested map destructuring might be slightly cleaner here.
  (let [db (-> :sys/db state :data-source)
        migrate (-> :sys/migrations state)]
    (migrate #'logging-ddl)
    (config-logging db)
    #()))

(comment
  *e

  (->> AbstractAppender
       cr/reflect
       :members
       (filter #(contains? (:flags %) :public))
       pp/print-table)

  (->> AbstractAppender
       cr/reflect
       :constructor
       (filter #(contains? (:flags %) :protected))
       pp/print-table)

  ()

  (require '[sys-loader.db :refer [mk-datasource]])

  (def ds (mk-datasource))

  (config-logging ds)

  (log/info "Hello!--")

  (time (log/info "Hello3"))

  (split "/usr/var/lib/x.clj" #"/")

  (config-logging nil)

  ;;
  )