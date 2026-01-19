(ns sys-loader.db
  "Relational DB Support"
  (:require [next.jdbc :as jdbc]
            [clojure.tools.logging :as log])
  (:import [java.net ServerSocket]
           [org.h2.tools Server]
           [org.h2.jdbcx JdbcConnectionPool]))

(defn port-in-use?
  "Checks if a given TCP port is already in use on localhost."
  [port]
  (try 
    (with-open [socket (ServerSocket. port)]
      (.setReuseAddress socket true) 
      false)
    (catch java.io.IOException _
      true)))

(defn h2-port []
  (or (System/getProperty "sys-loader.h2-port") "9092"))

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
                                       (h2-port)])
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

(def no-op-server 
  "A server which does nothing. It is assumed that another sys-loader may
   already be running the H2 TCP server."
  (fn [operation & args]
    {:pre [(#{:start :stop :server :info} operation)]}
    (case operation
      :start  (log/info "no-op server start")
      :stop   (log/info "no-op server stop")
      :server (log/info "no-op server server")
      :info   (log/info "no-op server info"))))

(defn init [_]
  ;; Note: it is possible that another sys-loader instance is already running
  ;; the H2 TCP server. In that case, we do not attempt to start another
  (let [server (if (-> (h2-port) Integer/parseInt port-in-use?)
                 no-op-server
                 (mk-h2-server :main-db))]
    (try
      (log/info "Attempting to start H2 DB server...")
      (server :start)
      (log/info "DB started successfully") 
      (catch java.net.BindException _ 
        (log/warn "DB server already running - proceeding"))
      (catch Throwable e
        (log/error e)))
    {:server      server
     :data-source (mk-datasource)}))


(comment
  *e
  (port-in-use? 9092)
  (-> (h2-port) Integer/parseInt port-in-use?)
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