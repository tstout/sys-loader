(ns user
  (:require [clojure.pprint :as pprint]
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

