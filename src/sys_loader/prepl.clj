(ns sys-loader.prepl
  (:require [clojure.core.server :refer [start-server stop-servers]]
            [taoensso.timbre :as log]))

(defn start-repl!
  "Start a prepl server based on the specified options.
   Returns the repl's server socket."
  [opts]
  (log/infof "attempting to start prepl...")
  (let [{:keys [bind-addr port]} opts
        server (start-server {:accept 'clojure.core.server/io-prepl
                              :address bind-addr
                              :port port
                              :name "jvm"})]
    (log/infof "Started prepl - %s:%d" bind-addr port)
    server))

(defn mk-repl
  "Create a prepl server which can be started and stopped.
   Returns a function which accepts the operations
   :start
   :stop
   :state"
  [opts]
  (let [server-sock (atom nil)
        repl-ops {:start (fn []
                           (when @server-sock (stop-servers))
                           (reset! server-sock (start-repl! opts)))
                  :stop (fn [] (when @server-sock
                                 (stop-servers)
                                 (reset! server-sock nil)))
                  :state (fn [] (if @server-sock :running :idle))}]
    (fn [operation]
      {:pre [(#{:start :stop :state} operation)]}
      ((-> operation repl-ops)))))


(defn init [_]
  #_(start-repl! {:bind-addr "localhost" :port 8000})
  (let [repl-fn (mk-repl {:bind-addr "localhost" :port 8000})]
    (repl-fn :start)
    repl-fn))

(comment
  *e
  (def repl (init {}))
  (repl :state)
  (repl :start)
  (repl :stop)
  (repl :foo)
  ;;
  )