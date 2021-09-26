(ns sys-loader.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [taoensso.timbre :as log]
            [clojure.java.io :as io]
            [sys-loader.plugin :refer [load-plugins-in-order!]])
  (:import [java.time Instant Duration]))

(def get-time
  (memoize (fn [_] (Instant/now))))

(defn t-diff-ms [a b]
  (-> (Duration/between (get-time a) (get-time b))
      .toMillis))

(defn -main [& args]
  (get-time :start)
  (load-plugins-in-order!)
  (get-time :end)
  (log/infof "sys-loader started in %s ms" (t-diff-ms :start :end))
  (log/info (-> "logo.txt" io/resource slurp)))


(comment
  *e
  (get-time :foo)
  (get-time :bar)

  (ns-publics (find-ns 'sys-loader.plugin))

  (t-diff-ms (get-time :foo) (get-time :bar))

  (log/info (-> "logo.txt" io/resource slurp))

  (-> "logo.txt" io/resource slurp)
  ;;
  )
