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
        (throw (Exception. "Circular or undefined dependency"))))))

(defn build-deps
  "Merge all plugin name and deps into a single collection suitable for processing by
   order-deps"
  [deps]
  (->>
   deps
   (map
    #_#(select-keys % [:sys/name :sys/deps])
    (fn [coll] {(:sys/name coll) (:sys/deps coll [])}))
   (apply merge)))

(comment
  (def p-def [{:sys/description "example plugin"
               :sys/name :loader
               :sys/deps [:service-a]
               :sys/init 'sys-loader.core-test/init}
              {:sys/description "example plugin2"
               :sys/name :service-a
               :sys/deps []
               :sys/init 'sys-loader.core-test/init}])

  (build-deps p-def)

  (-> p-def
      build-deps
      order-deps)

  (order-deps {:db [] :log [:db] :service-1 [:log :service-2] :service-2 [:log]})

  (order-deps {:log [:db] :db []})
  ;;
  )