(ns sys-loader.logging
  (:require [clojure.tools.logging :as log]
            [clojure.string :refer [split]]))

(defn logging-ddl [run-ddl]
  (run-ddl "logging"))

(defn log4j2-ddl [run-ddl]
  (run-ddl "log4j2"))

(defn init [state]
  ;; TODO nested map destructuring might be slightly cleaner here.
  (let [;;db (-> :sys/db state :data-source)
        migrate (-> :sys/migrations state)]
    (migrate #'logging-ddl
             #'log4j2-ddl)
    (log/info "Logging Initialized")
    #()))

(comment
  *e
  (require '[sys-loader.db :refer [mk-datasource]])

  (def ds (mk-datasource))

  (log/info "Hello!--")

  (time (log/info "Hello3"))

  (split "/usr/var/lib/x.clj" #"/")
  ;;
  )