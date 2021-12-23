(ns user
  (:require [clojure.pprint :as pprint]
            [sys-loader.core :refer [sys-state version-str]]
            [sys-loader.module :refer [module-cfg]]
            [clojure.repl :refer [dir source doc]]))

(defn ls
  "Print a listing of all the loaded modules."
  []
  (pprint/print-table
   [:plugin :description]
   (map (fn [x] {:module x
                 :description
                 (-> (filter #(= x (:sys/name %)) @module-cfg)
                     first
                     :sys/description)})
        (keys @sys-state))))

(defn loader
  "Execute a sys-loader command corresponding to the specified op.
   Supported ops:
   :ls       - list all loaded modules
   :version  - show sys-loader version"
  [op]
  (case op
    :ls (ls)
    :version @version-str))

(defn help []
  (doc loader))

(comment
  *e
  (doc help)

  (source clojure-version)

  @module-cfg
  (ls)
  (source ls)
  (doc ls)
  (dir user)
  (filter #(= :sys/db (:sys/name %)) @module-cfg)
  (-> (filter #(= :sys/db (:sys/name %)) @module-cfg) first :sys/description)
  ;;
  )