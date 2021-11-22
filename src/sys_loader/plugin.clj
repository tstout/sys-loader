(ns sys-loader.plugin
  (:require [clojure.edn :as edn]
            [clojure.string :refer [split]]
            [taoensso.timbre :as log]
            [sys-loader.deps :refer [order-deps build-deps]]
            [sys-loader.plugin-spec]
            [clojure.spec.alpha :as s]))

(def intrinsics
  "Define plugins that are baked-in to sys-loader"
  [{:sys/description "Database"
    :sys/name :sys/db
    :sys/deps []
    :sys/init 'sys-loader.db/init}
   {:sys/description "Logging"
    :sys/name :sys/logging
    :sys/deps [:sys/db :sys/migrations]
    :sys/init 'sys-loader.logging/init}
   {:sys/description "Migrations"
    :sys/name :sys/migrations
    :sys/deps [:sys/db]
    :sys/init 'sys-loader.migrations/init}
   {:sys/description "Prepl"
    :sys/name :sys/prepl
    :sys/deps [:sys/logging]
    :sys/init 'sys-loader.prepl/init}])

(defn load-plugin-cfg
  "Traverse the resources in the classpath, looking for plugin.edn files.
   Returns a sequence of all plugin definitions found."
  []
  (let [plugins (.getResources (ClassLoader/getSystemClassLoader) "plugin.edn")]
    (loop [output []]
      (if (.hasMoreElements plugins)
        #_(recur (conj output (edn/read-string (slurp (.. plugins nextElement openStream)))))
        ;; TODO - is this a resource leak here?
        (recur (->> (.. plugins nextElement openStream)
                    slurp
                    edn/read-string
                    (conj output)))
        (-> intrinsics (conj output) flatten)))))

;; TODO - make this reloadable...maybe using compare-and-set! with an atom
(def plugin-cfg
  "A delay containing the aggregate plugin configurations obtained from the classpath.
   Configurations are validated against the spec :sys/plugin. Any invalid configurations are
   logged and ignored."
  (delay
   (let [[valid invalid] (split-with #(s/valid? :sys/plugin %) (load-plugin-cfg))]
     (doseq [interloper invalid]
       (log/errorf "Invalid plugin config: %s" (s/explain-str :sys/plugin interloper)))
     valid)))

(defn load-plugin
  "Given a plugin map (as defined in the spec :sys/plugin), load the plugin and invoke its
   init function passing it the current system state."
  [plugin state]
  (let [{:keys [sys/description sys/init sys/name]} plugin]
    (log/infof "loading module: %s %s %s" name init description)
    (-> init
        str
        (split #"/")
        first
        symbol
        require)
    ((resolve init) state)))

(defn find-by-name [name plugins]
  (->> plugins
       (filter #(= name (:sys/name %)))
       first))

(defn load-plugins-in-order!
  "Search the classpath for resources files named plugin.edn. For each plugin configuration found,
   Invoke the init function in the appropriate order as specified by any dependencies listed.
   The init function can return some state, which is merged into a map and successivley passed to 
   init functions. Returns the merged results from all init functions."
  []
  (let [plugins @plugin-cfg
        deps (-> plugins
                 build-deps
                 order-deps)]
    (reduce (fn [accum plugin-name]
              (let [cfg (find-by-name plugin-name plugins)]
                (merge accum {plugin-name (load-plugin cfg accum)})))
            {}
            deps)))

(comment
  *e
  @plugin-cfg
  intrinsics
  (load-plugin-cfg)
  (load-plugins-in-order!)

  (def deps (-> (load-plugin-cfg) build-deps order-deps))

  (-> (load-plugin-cfg) build-deps)
  (flatten (conj [{:a 1}] [{:b 1} {:c 1}]))

  (split-with #(s/valid? :sys/plugin %) intrinsics)

  (s/explain-data :sys/plugin {:sys/description "Migrations"
                               :sys/name :sys/migrations
                               :sys/deps [""]
                               :sys/init 'sys-loader.migrations/init})

  (s/explain-str :sys/plugin {:sys/description "Migrations"
                              :sys/name :sys/migrations
                              :sys/deps [""]
                              :sys/init 'sys-loader.migrations/init})

  ;;
  )