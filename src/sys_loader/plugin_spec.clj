(ns sys-loader.plugin-spec
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

(s/def :sys/plugin (s/keys :req [:sys/description
                                 :sys/name
                                 :sys/deps
                                 :sys/init]))

(s/def :sys/plugins (s/coll-of :sys/plugin))

(comment
  *e
  (s/describe :sys/plugin)
  (s/valid? :sys/description 1)
  (s/explain-data :sys/deps [:a "s"])
  (s/explain :sys/deps [:a "s"])

  (s/valid? :sys/plugin {:sys/description "Database"
                         :sys/name :sys/db
                         :sys/deps []
                         :sys/init 'sys-loader.db/init})

  (s/valid? :sys/plugins [{:sys/description "Database"
                           :sys/name :sys/db
                           :sys/deps []
                           :sys/init 'sys-loader.db/init}])

  (require '[clojure.spec.gen.alpha :as gen])

  (gen/generate (s/gen :sys/plugin))
  (gen/generate (s/gen :sys/plugins))

  #_{:sys/description "Database"
     :sys/name :sys/db
     :sys/deps []
     :sys/init 'sys-loader.db/init}
  ;;
  )

