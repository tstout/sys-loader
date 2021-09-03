(ns sys-loader.plugin
  (:require [clojure.edn :as edn]
            [clojure.string :as s]
            [taoensso.timbre :as log]))

;; (defn load-plugin [url]
;;   (let [{:keys [sys/description sys/init sys/deps]} (edn/read-string (slurp url))]
;;     (log/infof "loading module: %s" description)
;;     (-> init
;;         str
;;         (s/split #"/")
;;         first
;;         symbol
;;         require)
;;     ((resolve init))))



;; (defn init-plugins [init-fns]
;;   (resolve init-fn))

;; TODO - name of this fn should probably be read-plugin-cfg
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

(defn load-plugins-in-order []
  (doseq [plugin (load-plugin-cfg)]
    (load-plugin plugin)))


(comment
  (load-plugin-cfg)
  (load-plugins-in-order)
  (flatten [{:a 1} [{:d 1} {:e 1}]])

  ;;
  )