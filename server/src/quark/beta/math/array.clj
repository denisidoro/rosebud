(ns quark.beta.math.array
  (:require [kixi.stats.core :as stats]))

(defn ^:private from-kixi-stats
  [f xs]
  (transduce identity f xs))

(def mean (partial from-kixi-stats stats/mean))
(def median (partial from-kixi-stats stats/median))
