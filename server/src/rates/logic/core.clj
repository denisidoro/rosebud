(ns rates.logic.core
  (:require [clojure.string :as str]
            [common.config :as config]
            [quark.beta.math.core :as math2]
            [quark.beta.time :as time]
            [quark.math.core :as math]))

;; Math

(defn find-beta
  [points alpha r]
  (/ (math2/log alpha r)
     points))

(defn ^:private distribution
  [alpha beta x]
  (math/pow alpha (* beta x)))

(defn ^:private as-symbols
  [currencies]
  (->> currencies
       (map name)
       (map str/upper-case)
       (str/join ",")))

;; Time

(defn millis->date-str
  [millis]
  (-> millis
      time/from-millis
      time/date-time->string))

(defn date-str
  [x alpha beta now]
  (as-> x it
    (distribution alpha beta it)
    (int it)
    (- now (* it 1000 60 60 24))
    (millis->date-str it)))

(defn date-strs
  ([now] (date-str now 1.8 1.2 12))
  ([now alpha beta points]
   (->> (range 0 points)
        (map #(date-str % alpha beta now)))))

;; Adapter

(defn ^:private currency-reducer
  [acc
   {:keys [rates date]}]
  (let [millis (time/as-millis date)]
    (reduce
     (fn [transient-acc [k v]]
       (assoc-in transient-acc [k millis] (/ 1 v)))
     acc
     rates)))

(defn ^:private as-currency-bucket
  [[symbol history]]
  (let [path [:currency (-> symbol name str/lower-case keyword)]
        id (->> path (map name) (apply keyword))]
    {:bucket/id id
     :bucket/path path
     :history/gross history}))

(defn bodies->buckets
  [bodies]
  (->> bodies
       (reduce currency-reducer {})
       (map as-currency-bucket)))

;; HTTP

(defn replace-map
  [currencies base-currency millis]
  {:date    (millis->date-str millis)
   :base    base-currency
   :symbols (as-symbols currencies)})
