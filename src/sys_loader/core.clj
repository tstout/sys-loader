(ns sys-loader.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.tools.logging :as log]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [sys-loader.module :refer [load-modules-in-order!]])
  (:import [java.time Instant Duration]
           [com.github.tstout.sysloader H2ConnFactory
            H2ConnFactory$Singleton])
  (:gen-class))

(def version-str
  (delay (-> "sys-loader.edn"
             io/resource
             slurp
             edn/read-string
             :version)))

(def get-time
  (memoize (fn [_] (Instant/now))))

(defn t-diff-ms [a b]
  (-> (Duration/between (get-time a) (get-time b))
      .toMillis))

(def sys-state
  "The state of the system. This is a map of all modules that have been loaded.
   The keys are the module's keyword name as defined in each module.edn file found
   on the classpath."
  (delay (load-modules-in-order!)))

(defn create-ds []
  (->  @sys-state
       :sys/db
       :data-source))

(defn -main [& args]
  (get-time :start)
  @sys-state
  (get-time :end)
  (log/infof "sys-loader started in %s ms" (t-diff-ms :start :end))
  (log/info (-> "logo.txt"
                io/resource
                slurp
                (str @version-str))))


(comment
  *e
  (macroexpand '(->
                 (@sys-state :sys/db)
                 :data-source))


  (create-ds)

  @version-str
  (-main [])
  (get-time :foo)
  (get-time :bar)

  ;;H2ConnFactory$Singleton

  (ns-publics (find-ns 'sys-loader.module))

  (t-diff-ms (get-time :foo) (get-time :bar))

  (log/info (-> "logo.txt" io/resource slurp))

  (-> "logo.txt" io/resource slurp)
  ;;
  )
