(ns sys-loader.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [taoensso.timbre :as log]
            [sys-loader.plugin :refer [load-plugins-in-order!]]))

(defn init [_]
  (log/info "-----Plugin Init---------"))


(defn -main [& args]
  (load-plugins-in-order!))
