(ns quark.beta.collection
  (:require [quark.collection.map :as map]))

(defn safe-get
  [coll key & args]
  (if (coll? key)
    (apply get-in coll (vec key) args)
    (apply get coll key args)))

(defn fill-map
  ([map-interpolation-fn m ks]
   (fill-map map-interpolation-fn identity identity m ks))
  ([map-interpolation-fn x-serialize-fn x-unserialize-fn m ks]
   (let [adapt? (and x-serialize-fn (not (= identity x-serialize-fn)))
         num-m  (if adapt?
                  (into (sorted-map) (map/map-keys x-serialize-fn m))
                  m)
         num-ks (if adapt?
                  (map x-serialize-fn ks)
                  ks)
         result (->> num-ks
                     (reduce (fn [m x]
                               (if (get m x)
                                 m
                                 (assoc m x (map-interpolation-fn m x))))
                             num-m))]
     (if adapt?
       (->> result
            (map/map-keys x-unserialize-fn)
            (into (sorted-map)))
       result))))
