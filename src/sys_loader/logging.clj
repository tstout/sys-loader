(ns sys-loader.logging
  (:require [clojure.tools.logging :as log]
            [clojure.string :refer [split]]))

(defn log4j2-ddl [run-ddl]
  (run-ddl "log4j2"))

(defn log4j2-app-name-ddl [run-ddl]
  (run-ddl "log4j2-app-name"))

(defn log4j2-thread-name-ddl [run-ddl]
  (run-ddl "log4j2-thread-name"))

(defn init [state]
  
  (let [;;db (-> :sys/db state :data-source)
        migrate (-> :sys/migrations state)]
    (migrate #'log4j2-ddl 
             #'log4j2-app-name-ddl
             #'log4j2-thread-name-ddl)
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