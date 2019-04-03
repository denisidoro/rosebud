(ns quark.beta.math.curve
  (:require [quark.beta.math.point :as point]
            [quark.collection.map :as map]
            [quark.math.core :as math]))

(defn mcurve
  [curve]
  (into (sorted-map) curve))

;; TODO: stop relying on mcurve conversion
(defn linear-interpolate
  [curve x]
  (let [m               (mcurve curve)
        ks              (keys m)
        ks-count        (count ks)
        partitions      (partition-by #(compare x %) ks)
        partition-count (count partitions)
        single-value?   (= 1 ks-count)
        exact-match?    (= partition-count 3)
        two-values?     (= 2 ks-count)
        outside?        (= partition-count 1)
        smallest?       (and outside?
                             (< x (ffirst partitions)))
        biggest?        (and outside?
                             (not smallest?))
        zeroed?         (or (and biggest?
                                 (-> m vals last (= 0)))
                            (and smallest?
                                 (-> m vals first (= 0))))]
    (cond
      zeroed?
      0

      single-value?
      (-> m vals first)

      smallest?
      (-> m vals first)

      biggest?
      (-> m vals last)

      exact-match?
      (get m x)

      :else
      (let [[x0 x1] (cond
                      two-values? (->> partitions flatten (take 2))
                      ; smallest? (->> partitions first (take 2))
                      ; biggest? (->> partitions last (take-last 2))
                      :else [(-> partitions first last)
                             (-> partitions last first)])
            y0 (get m x0)
            y1 (get m x1)]
        (math/linear-interpolation x0 y0 x1 y1 x)))))

(def ^:private default-opts
  {:interpolator linear-interpolate})

(defn xs
  [curve]
  (mapv point/x curve))

(defn ys
  [curve]
  (mapv point/y curve))

(defn fill-curve-for-all-xs
  ([curve xs] (fill-curve-for-all-xs default-opts curve xs))
  ([{:keys [interpolator]} curve xs]
   (mapv (fn [x] [x (interpolator curve x)]) xs)))

(defn fill-curve-with-x
  ([curve x] (fill-curve-with-x default-opts curve x))
  ([opts curve x]
   (fill-curve-for-all-xs opts curve (sort (conj (xs curve) x)))))

;; TODO: add heuristics for speed
;; are all keys equal?
(defn fill-curves
  ([curves] (fill-curves default-opts curves))
  ([opts curves]
   (let [xs (->> curves (mapcat xs) sort dedupe)]
     (mapv #(fill-curve-for-all-xs opts % xs) curves))))

(defn operate
  ([f curves] (operate f default-opts curves))
  ([f opts curves]
   (let [curves' (fill-curves opts curves)
         xs      (->> curves' first xs)
         yss     (map ys curves')]
     (apply mapv (fn [x & ys] [x (apply f ys)]) xs yss))))

(def sum
  (partial operate +))

(def subtract
  (partial operate -))

(defn derivative
  [curve]
  (loop [[x & xs] (xs curve)
         [y & ys] (ys curve)
         acc []]
    (if (empty? xs)
      acc
      (let [[next-x] xs
            [next-y] ys
            v    (/ (- next-y y)
                    (- next-x x))
            acc' (conj acc [x v])]
        (recur xs ys acc')))))

(defn percentage
  ([curve]
   (let [base-y (-> curve first point/y)]
     (mapv (fn [[x y]] [x (/ y base-y)]) curve)))
  ([curve min-x]
   (let [base-y (->> curve (map/find-first (fn [[x _]] (>= x min-x))) point/y)]
     (mapv (fn [[x y]] [x (/ y base-y)]) curve))))

(defn accumulate
  [curve]
  (loop [[x & xs] (xs curve)
         [y & ys] (ys curve)
         acc []]
    (if-not x
      acc
      (let [previous-sum (or (some->> acc last point/y) 0)
            sum          (+ previous-sum y)
            acc'         (conj acc [x sum])]
        (recur xs ys acc')))))

(defn operate-over-ys
  [f curve]
  (->> curve
       ys
       f))

(defn operate-over-xs
  [f curve]
  (->> curve
       xs
       f))
