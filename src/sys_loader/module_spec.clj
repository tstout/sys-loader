(ns sys-loader.module-spec
  "Define specs for a plugin definition.
   
   Here is an example plugin def:
   {:sys/description \"Database\"
     :sys/name :sys/db
     :sys/deps []
     :sys/init 'sys-loader.db/init}
   "
  (:require [clojure.spec.alpha :as s]))

(s/def :sys/description string?)
(s/def :sys/name keyword?)
(s/def :sys/deps (s/coll-of keyword?))
(s/def :sys/init symbol?)

(s/def :sys/module (s/keys :req [:sys/description 
                                 :sys/name
                                 :sys/deps
                                 :sys/init]))

(s/def :sys/modules (s/coll-of :sys/module))

(comment
  *e
  (s/describe :sys/module)
  (s/valid? :sys/description 1)
  (s/explain-data :sys/deps [:a "s"])
  (s/explain :sys/deps [:a "s"])


  (s/conform :sys/module {:sys/description "Database"
                          :sys/name :sys/db
                          :sys/deps []
                          :sys/init 'sys-loader.db/init})

  (s/valid? :sys/module {:sys/description "Database"
                         :sys/name :sys/db
                         :sys/deps []
                         :sys/init 'sys-loader.db/init})

  (s/valid? :sys/modules [{:sys/description "Database"
                           :sys/name :sys/db
                           :sys/deps []
                           :sys/init 'sys-loader.db/init}])

  (require '[clojure.spec.gen.alpha :as gen])

  (gen/generate (s/gen :sys/module))
  (gen/generate (s/gen :sys/modules))

  #_{:sys/description "Database"
     :sys/name :sys/db
     :sys/deps []
     :sys/init 'sys-loader.db/init}
  ;;
  )

