(ns rosebud.logic.balance-map
  (:require [quark.beta.math.point :as point]
            [quark.collection.map :as map]))

(defn remove-empty
  [balance-map]
  (map/filter-vals #(-> % first point/x pos?) balance-map))

(defn remove-current-zero
  [balance-map]
  (map/filter-vals #(-> % last point/y zero?) balance-map))
