(ns user
  (:require [clojure.pprint :as pprint]
            [sys-loader.core :refer [sys-state version-str]]
            [sys-loader.plugin :refer [plugin-cfg]]
            [clojure.repl :refer [dir source doc]]))

;; Trace fns borrowed from https://lambdaisland.com/blog/2019-12-17-advent-of-parens-17-trace-untrace
(defn trace! [v]
  (let [m    (meta v)
        n    (symbol (str (ns-name (:ns m))) (str (:name m)))
        orig (:trace/orig m @v)]
    (alter-var-root v (constantly (fn [& args]
                                    (prn (cons n args))
                                    (apply orig args))))
    (alter-meta! v assoc :trace/orig orig)))

(defn untrace! [v]
  (when-let [orig (:trace/orig (meta v))]
    (alter-var-root v (constantly orig))
    (alter-meta! v dissoc :trace/orig)))


;;(add-tap (bound-fn* pprint/pprint))

(defn ls
  "Print a listing of all the loaded plugins."
  []
  (pprint/print-table
   [:plugin :description]
   (map (fn [x] {:plugin x
                 :description
                 (-> (filter #(= x (:sys/name %)) @plugin-cfg)
                     first
                     :sys/description)})
        (keys @sys-state))))

(defn loader
  "Execute a sys-loader command corresponding to the specified op.
   Supported ops:
   :ls       - list all loaded plugins
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

  @plugin-cfg
  (ls)
  (source ls)
  (doc ls)
  (dir user)
  (filter #(= :sys/db (:sys/name %)) @plugin-cfg)
  (-> (filter #(= :sys/db (:sys/name %)) @plugin-cfg) first :sys/description)
  ;;
  )