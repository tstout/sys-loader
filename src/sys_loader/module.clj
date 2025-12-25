(ns sys-loader.module
  (:require [clojure.edn :as edn]
            [clojure.string :refer [split]]
            [clojure.tools.logging :as log]
            [sys-loader.deps :refer [order-deps build-deps]]
            [sys-loader.module-spec]
            [clojure.spec.alpha :as s]))

(def intrinsics
  "Define modules that are baked-in to sys-loader"
  [#_{:sys/description "Database"
      :sys/name :sys/db
      :sys/deps []
      :sys/init 'sys-loader.db/init}
   #_{:sys/description "Logging"
      :sys/name :sys/logging
      :sys/deps [:sys/migrations]
      :sys/init 'sys-loader.logging/init}
   #_{:sys/description "Migrations"
      :sys/name :sys/migrations
      :sys/deps []
      :sys/init 'sys-loader.migrations/init}
   {:sys/description "Prepl"
    :sys/name :sys/prepl
    :sys/deps []
    :sys/init 'sys-loader.prepl/init}])

(defn load-module-cfg
  "Traverse the resources in the classpath, looking for module.edn files.
   Returns a sequence of all module definitions found."
  []
  (let [modules (.getResources (ClassLoader/getSystemClassLoader) "module.edn")]
    (loop [output []]
      (if (.hasMoreElements modules)
        #_(recur (conj output (edn/read-string (slurp (.. plugins nextElement openStream)))))
        ;; TODO - is this a resource leak here?
        (recur (->> (.. modules nextElement openStream)
                    slurp
                    edn/read-string
                    (conj output)))
        (-> intrinsics (conj output) flatten)))))

(defn prn-modules
  "Print modules from class path. Intended for repl debugging of classpath issues."
  []
  (let [modules (.getResources (ClassLoader/getSystemClassLoader) "module.edn")]
    (while (.hasMoreElements modules)
      (prn (str ">>>Module: "  (.getName (Thread/currentThread)) " " (.. modules nextElement))))))

;; TODO - make this reloadable...maybe using compare-and-set! with an atom
(def module-cfg
  "A delay containing the aggregate module configurations obtained from the classpath.
   Configurations are validated against the spec :sys/module. Any invalid configurations are
   logged and ignored."
  (delay
    (let [[valid invalid] (split-with #(s/valid? :sys/module %) (load-module-cfg))]
      (doseq [interloper invalid]
        (log/errorf "Invalid module config: %s" (s/explain-str :sys/module interloper)))
      #_(prn "valid-moduels>>> " valid)
      valid)))

(defn invoke-pre-init [pre-fn-name state-map]
  (log/infof "invoking pre-init function: %s" pre-fn-name)
  (try
    (-> pre-fn-name
        str
        (split #"/")
        first
        symbol
        require) 
    ((resolve pre-fn-name) state-map)
    (catch Exception e
      (log/error e))))

(defn do-pre-init 
  "Invoke the pre-init function for a module if it is defined.
   Returns the state map (possibly updated)."
  [module state-map] 
  (let [{:keys [sys/pre-init]} module] 
    (if pre-init
      (merge state-map (invoke-pre-init pre-init state-map))
      state-map)))

(defn do-pre-inits
  "Invoke any pre-init functions defined in the module configurations.
   Pre-init functions are invoked before any modules are initialized.
   This is intended to allow modules to provide configuration to any dependent
   modules before those modules are initialized. Any map returned from the 
   pre-init functions are merged into the system state map."
  [modules state-map] 
  (reduce (fn [accum module]
            (merge accum (do-pre-init module accum)))
          state-map
          modules))

(defn load-module
  "Given a module map (as defined in the spec :sys/module), load the module and invoke its
   init function passing it the current system state."
  [module state]
  (let [{:keys [sys/description sys/init sys/name]} module]
    #_(prn "LOADING MODULE " module)
    (log/infof "loading module: %s %s %s" name init description)
    (try
      (-> init
          str
          (split #"/")
          first
          symbol
          require)
      ((resolve init) state)
      (catch Exception e
        (log/error e)))))

(defn find-by-name [name modules]
  (->> modules
       (filter #(= name (:sys/name %)))
       first))

(defn load-modules-in-order!
  "Search the classpath for resources files named module.edn. For each module configuration found,
   Invoke the init function in the appropriate order as specified by any dependencies listed.
   The init function can return some state, which is merged into a map and successivley passed to 
   init functions. Returns the merged results from all init functions."
  [init-state]
  #_(prn "Load modules in order!!!!!!! Thread " (.getName (Thread/currentThread)))
  (let [modules          @module-cfg
        pre-init-results (do-pre-inits modules init-state) 
        deps             (-> modules
                             build-deps
                             order-deps)]
    #_(prn "DEPS>>>>>: " deps)
    #_(prn-modules)
    (reduce (fn [accum module-name]
              (let [cfg (find-by-name module-name modules)]
                (merge accum {module-name (load-module cfg accum)})))
            pre-init-results
            deps)))

(comment
  *e
  
  @module-cfg


  (->>  @module-cfg
        (filter #(contains? % :sys/init))
        (map :sys/name))

  (prn-modules)

  (macroexpand '@module-cfg)

  intrinsics
  (load-module-cfg)

  (def deps (-> (load-module-cfg) build-deps order-deps))

  (-> (load-module-cfg) build-deps)

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