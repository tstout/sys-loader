(ns sys-loader.db
  (:require [next.jdbc :as jdbc])
  (:import (java.net InetAddress)
           (org.h2.tools Server)
           (org.h2.jdbcx JdbcConnectionPool)))

(defn host-name []
  (.. InetAddress getLocalHost getHostName))

(def jdbcUrls
  {:memory ""
   :server (str "jdbc:h2:tcp://"
                (host-name)
                "/~/.sys-loader/db/sys-loader;jmx=true")})

(defn mk-datasource []
  (JdbcConnectionPool/create (:server jdbcUrls) "sa" ""))

(defn mk-h2-server
  "Create an H2 server on port 9092. Returns a function which accepts the operations
  :start and :stop"
  []
  (let
   [server (->
            ;; TODO - revisit the -ifNotExists setting
            (into-array String ["-tcpAllowOthers" "-ifNotExists"])
            Server/createTcpServer)
    server-ops {:start (fn [] (.start server))
                :stop  (fn [] (.stop server))}]
    (fn [operation & args] (-> (server-ops operation) (apply args)))))

(defn init [_]
  (let [server (mk-h2-server)]
    (server :start)
    {:server server
     :data-source (mk-datasource)}))


(comment
  *e
  (def state (init {}))

  state

  ((-> :server state) :stop)

  (def server (mk-h2-server))
  (server :start)
  (server :stop)

  (def ds (mk-datasource))

  (def create-table "create table LOG (
   id        int identity(1, 1) primary key not null
  ,instant   datetime not null
  ,level     varchar(32) not null
  ,namespace varchar(1000)
  ,file      varchar(100)
  ,line      int
  ,msg       varchar(4096) not null
);")

  (jdbc/execute! ds [create-table])

  ;;
  )