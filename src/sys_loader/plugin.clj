(ns sys-loader.plugin
  (:require [clojure.edn :as edn]
            [clojure.string :as s]
            [taoensso.timbre :as log]
            [sys-loader.deps :refer [order-deps build-deps]]))

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
        (flatten output)))))

(defn load-plugin [plugin state]
  (let [{:keys [sys/description sys/init sys/name]} plugin]
    (log/infof "loading module: %s" name)
    (-> init
        str
        (s/split #"/")
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
   The init function can return some state, which is meged into a map and successivley passed to 
   init functions. This function returns the merged results from all init functions."
  []
  (let [plugins (load-plugin-cfg)
        deps (-> plugins
                 build-deps
                 order-deps)]
    (reduce (fn [accum plugin-name]
              (let [cfg (find-by-name plugin-name plugins)]
                (merge accum {plugin-name (load-plugin cfg accum)})))
            {}
            deps)))


(comment
  (load-plugin-cfg)
  (load-plugins-in-order!)

  (def deps (-> (load-plugin-cfg) build-deps order-deps))

  (-> (load-plugin-cfg) build-deps)
  ;;
  )