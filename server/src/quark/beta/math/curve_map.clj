(ns quark.beta.math.curve-map
  (:require [quark.beta.math.curve :as curve]
            [quark.collection.map :as map]))

(defn curves
  [curve-map]
  (vals curve-map))

(defn xs
  [curve-map]
  (->> curve-map
       curves
       (mapcat curve/xs)
       sort
       dedupe))

(defn ys
  [curve-map]
  (->> curve-map
       curves
       (mapcat curve/ys)
       sort
       dedupe))

(defn ks
  [curve-map]
  (keys curve-map))

(defn operate-over-curves
  [f curve-map]
  (->> curve-map
       curves
       (curve/operate f)))

(def sum
  (partial operate-over-curves +))

(def subtract
  (partial operate-over-curves -))

(defn fill-with-last-x
  [curve-map]
  (let [last-x (-> curve-map xs sort last)]
    (map/map-vals #(curve/fill-curve-with-x % last-x) curve-map)))

