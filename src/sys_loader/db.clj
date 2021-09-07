(ns sys-loader.db
  (:import (java.net InetAddress)
           (org.h2.tools Server)
           (org.h2.jdbcx JdbcConnectionPool)))

(defn mk-h2-server
  "Create an H2 server. Returns a function which accepts the operations
  :start and :stop"
  []
  (let
   [server (->
            (into-array String ["-tcpAllowOthers"])
            Server/createTcpServer)
    server-ops {:start (fn [] (.start server))
                :stop  (fn [] (.stop server))}]
    (fn [operation & args] (-> (server-ops operation) (apply args)))))

(comment
  (def server (mk-h2-server))
  (server :start)
  (server :stop)
  ;;
  )