(ns sys-loader.prepl
  (:require [clojure.core.server :refer [start-server]]
            [taoensso.timbre :as log]))

(defn start-repl! [opts]
  (log/infof "attempting to start prepl...")
  (let [{:keys [bind-addr port]} opts
        server (start-server {:accept 'clojure.core.server/io-prepl
                              :address bind-addr
                              :port port
                              :name "jvm"})]
    (log/infof "Started prepl - %s:%d" bind-addr port)
    server))

(defn init [state]
  (start-repl! {:bind-addr "localhost" :port 8000}))



(comment
  (init {})
  ;;
  )