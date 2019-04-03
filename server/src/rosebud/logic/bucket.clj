(ns rosebud.logic.bucket
  (:require [quark.beta.math.curve :as curve]
            [quark.beta.math.point :as point]
            [quark.beta.time :as time]
            [quark.collection.map :as map]
            [quark.collection.seq :as seq]
            [quark.navigation.core :as nav]))

(defn find-path
  [hierarchy leaf]
  (let [path-seqs (nav/path-seq hierarchy)
        {:keys [path form]} (seq/find-first #(-> % :form (= leaf)) path-seqs)
        path-seq  (when-not path
                    (seq/find-first (fn [{:keys [path]}] (some #(= % leaf) path)) path-seqs))]
    (if path-seq
      (->> path-seq :path reverse (drop-while #(not= % leaf)) reverse vec)
      (-> path drop-last vec (conj form)))))

(defn ^:private gross-history
  [id balances]
  (reduce
   (fn [acc [date m]]
     (let [amount (some->> m (map/filter-keys #(= % id)) vals first)]
       (if amount
         (conj acc (point/new (time/date-str->millis date) amount))
         acc)))
   []
   balances))

(defn with-interpolated-gross-history
  [xs
   {:history/keys [gross] :as bucket}]
  (assoc bucket :history/interpolated-gross (curve/fill-curve-for-all-xs gross xs)))

(defn with-gross-history
  [{:bucket/keys [id] :as bucket}
   curve-segment
   balance-history]
  (->> curve-segment
       (concat (gross-history id balance-history))
       (sort-by first)
       vec
       (assoc bucket :history/gross)))

(defn find-all-millis+end-of-months
  [now buckets]
  (let [xs        (->> buckets
                       (mapcat (fn [{:history/keys [gross]}]
                                 (map first gross))))
        first-x   (first xs)
        last-x    (last xs)
        one-month (* 1000 60 60 24 30)
        all-xs    (into [(- first-x one-month)
                         now
                         (+ last-x one-month)]
                        xs)]
    (-> all-xs sort vec)))

