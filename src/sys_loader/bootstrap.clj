(ns sys-loader.bootstrap
  (:require [sys-loader.db :as db]
            [sys-loader.migrations :as migration]
            [sys-loader.logging :as logging]
            [clojure.pprint :refer [pprint]]
            [sys-loader.module :refer [load-modules-in-order!]]))


(def boot
  (delay
    #_(prn ">>>>>>>> Calling Boot <<<<<<<<<<<<<")
    #_(clojure.pprint/pprint (-> (Throwable.) .getStackTrace seq))
    (let [h2-db {:sys/db (db/init {})}
          mig-fn {:sys/migrations (migration/init h2-db)}
          log-fn {:sys/logging (logging/init mig-fn)}]
      (merge h2-db mig-fn log-fn))))


(def sys-state
  "The state of the system. This is a map of all modules that have been loaded.
   The keys are the module's keyword name as defined in each module.edn file found
   on the classpath."
  (delay (load-modules-in-order! @boot)))

(defn create-ds []
  (->  @boot
       :sys/db
       :data-source))


(comment

  (-> (Throwable.) .getStackTrace seq)
    ;;
  )

