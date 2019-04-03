(ns quark.beta.math.point)

(defn x
  [point]
  (first point))

(defn y
  [point]
  (second point))

(defn new
  [x y]
  [x y])
