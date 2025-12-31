(ns sys-loader.db
  "Relational DB Support"
  (:require [next.jdbc :as jdbc]
            [clojure.tools.logging :as log])
  (:import [java.net InetAddress]
           [org.h2.tools Server]
           [org.h2.jdbcx JdbcConnectionPool]))

#_(defn host-name []
    (.. InetAddress getLocalHost getHostName))

(def jdbcUrls
  {:memory "jdbc:h2:mem:sys-loader;DB_CLOSE_DELAY=-1"
   :server (str "jdbc:h2:tcp://"
                "localhost"
                "/~/.sys-loader/db/sys-loader;jmx=true")})

;; TODO - support non-default password
;; TODO consider using *command-line-args* dynamic var
;; to pass H2 port override.
(defn mk-datasource
  "Create a datasource. With no arguments, assume server. For specific
  control specify :server or :memory as argument."
  ([]
   (mk-datasource :server))
  ([t]
   {:pre [(keyword? t) (#{:memory :server} t)]}
   ;;(log/infof "Creating data source for %s" (t jdbcUrls))
   (JdbcConnectionPool/create (t jdbcUrls) "sa" "")))

(def mk-h2-server
  "Create an H2 server on port 9092. Returns a function which accepts the operations
  :start  (start server listening on TCP port)
  :stop   (stop the server from listening)
  :server (return the underlying java server object)
  :info   (return a map of server details)"
  (memoize
   ;; TODO - memoize here is working around an issue I have not been able to solve yet. mk-h2-server
   ;; is being called multiple times during system bootstrap after some refactoring causing an 
   ;; address already in use exception.
   (fn [_]
     (let
      [server     (->
                   ;; TODO - revisit the -ifNotExists setting
                   (into-array String ["-tcpAllowOthers"
                                       "-ifNotExists"
                                       "-tcp"
                                       "-tcpPort"
                                       (or (System/getProperty "sys-loader.h2-port") "9092")])
                   Server/createTcpServer)
       state      (atom :idle)
       server-ops {:start  (fn [] (when (= :idle @state)
                                    (reset! state :running)
                                    (.start server)))
                   :stop   (fn [] (when (= :running @state)
                                    (reset! state :idle)
                                    (.stop server)))
                   :server (fn [] server)
                   :info   (fn [] (bean server))}]
       (fn [operation & args] (-> (server-ops operation) (apply args)))))))

(defn init [_]
  (let [server (mk-h2-server :main-db)]
    (try
      #_(prn ">>>>>DB INIT!<<<<<<")
      (server :start)
      (log/info "DB started successfully")
      (catch Exception e
        (log/error e)))
    {:server      server
     :data-source (mk-datasource)}))


(comment
  *e
  (def state (init {}))
  (def mem-ds (mk-datasource :memory))

  mem-ds

  ((-> :server state) :stop)

  (def server (mk-h2-server :main-db))
  (server :start)
  (server :stop)

  (server :info)
  server

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