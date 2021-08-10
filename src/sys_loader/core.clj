(ns sys-loader.core
  (:require [clojure.tools.cli :refer [parse-opts]]
             [clojure.edn :as edn]
             [clojure.string :as s]))

(defn load-plugin [url]
  (let [{:keys [description init]} (edn/read-string (slurp url))]
    (println (str "loading module: " description))
    (-> init str (s/split #"/") first symbol require)
    ((resolve init))))

(defn load-plugins []
  (let [plugins (.getResources (ClassLoader/getSystemClassLoader) "plugin.edn")]
    (loop []
      (load-plugin (.. plugins nextElement openStream))
      (when (.hasMoreElements plugins)
        (recur)))))

(defn init []
  (println "-----Plugin Init---------"))


(defn -main [& args]
  (load-plugins))
