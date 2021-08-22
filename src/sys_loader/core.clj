(ns sys-loader.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.edn :as edn]
            [clojure.string :as s]
            [taoensso.timbre :as log]))

(defn load-plugin [url]
  (let [{:keys [description init deps]} (edn/read-string (slurp url))]
    (log/infof "loading module: %s" description)
    (-> init
        str
        (s/split #"/")
        first
        symbol
        require)
    ((resolve init))))

(defn load-plugins []
  (let [plugins (.getResources (ClassLoader/getSystemClassLoader) "plugin.edn")]
    (loop []
      (load-plugin (.. plugins nextElement openStream))
      (when (.hasMoreElements plugins)
        (recur)))))

(defn init []
  (log/info "-----Plugin Init---------"))


(defn -main [& args]
  (load-plugins))
