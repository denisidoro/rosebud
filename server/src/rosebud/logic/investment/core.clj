(ns rosebud.logic.investment.core
  (:require [quark.collection.map :as map]
            [quark.collection.ns :as ns]
            [rosebud.logic.investment.log :as l.investment.log]))

(defn from-log
  [log now]
  (l.investment.log/from-log log now))

(defn investment?
  [x]
  (some-> x :investment/application-amount))

(defn yielding?
  [x]
  (some-> x :bucket/tags :lifecycle (= :yielding)))

(defn fixed-income?
  [investment]
  (some-> investment :bucket/tags :kind #{:cdb :lci :lca}))

(defn fixed-income-yielding
  [investments]
  (filter (every-pred yielding? fixed-income?) investments))

(defn next-to-mature
  [investments]
  (->> investments
       (filter yielding?)
       (sort-by :investment/maturity-date)
       first))

(defn as-tabular
  [investment]
  (-> (map/map-keys ns/unnamespaced investment)
      (dissoc :gross :path :tags :interpolated-gross :maturity-date :maturity-amount)))
