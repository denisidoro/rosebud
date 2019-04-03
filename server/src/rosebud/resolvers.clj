(ns rosebud.resolvers
  (:require [rosebud.components.bucket.protocols.provider :as p.bucket]
            [rosebud.logic.investment.core :as l.investment]))

(defn yielding-cdi-investments
  [{{:keys [bucket]} :components}]
  (->> (p.bucket/get-investments bucket)
       l.investment/fixed-income-yielding
       (map l.investment/as-tabular)))

(def queries
  {:cdi/yielding yielding-cdi-investments})

(def tags
  {:buckets [:a :b :c]})

