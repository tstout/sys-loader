(ns sys-loader.deps)

(defn find-a-node [deps already-have-nodes]
  (some (fn [[k v]]
          (when (empty? (remove already-have-nodes v)) k))
        deps))

(defn order-deps
  "Topological sort to determine proper dependency order.
   Based on this discussion: 
   https://groups.google.com/g/clojure/c/-sypb2Djhio/m/r9AzRpwTgRkJ"
  [deps]
  (loop [deps deps already-have-nodes #{} output []]
    (if (empty? deps)
      output
      (if-let [item (find-a-node deps already-have-nodes)]
        (recur
         (dissoc deps item)
         (conj already-have-nodes item)
         (conj output item))
        (throw (Exception. "Circular dependency or undefined dependency"))))))


(comment
  (order-deps {:db [] :log [:db] :service-1 [:log :service-2] :service-2 [:log]})

  (order-deps {:log [:db] :db []})

  ;;
  )