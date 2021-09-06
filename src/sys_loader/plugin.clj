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

(defn load-plugin [plugin]
  (let [{:keys [sys/description sys/init sys/deps]} plugin]
    (log/infof "loading module: %s" description)
    (-> init
        str
        (s/split #"/")
        first
        symbol
        require)
    ((resolve init))))

(defn find-by-name [name plugins]
  (->> plugins
       (filter #(= name (:sys/name %)))
       first))

(defn load-plugins-in-order []
  (let [plugins (load-plugin-cfg)
        deps (-> plugins
                 build-deps
                 order-deps)]
    (doseq [plugin deps]
      (load-plugin (find-by-name plugin plugins)))))


(comment
  (load-plugin-cfg)
  (load-plugins-in-order)

  (def deps (-> (load-plugin-cfg) build-deps order-deps))

  (-> (load-plugin-cfg) build-deps)
  ;;
  )