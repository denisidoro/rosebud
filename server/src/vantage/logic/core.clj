(ns vantage.logic.core
  (:require [clojure.string :as str]
            [quark.beta.collection.map :as map2]
            [quark.beta.time :as time]
            [quark.collection.map :as map]
            [quark.conversion.data :as conversion]))

(defn ^:private get-series
  [body]
  (as-> body it
    (map2/get-by-substr it "eries")
    (map/map-keys #(some-> % name time/date-str->millis) it)
    (map/map-vals (fn [v] (some-> v (map2/get-by-substr "close") bigdec)) it)))

(defn ^:private get-history
  [body]
  (->> body
       get-series
       (into [])
       (sort-by first)))

(defn ^:private get-symbol
  [body currency?]
  (-> body
      (map2/get-in-by-substr ["eta" (if currency? "To Symbol" "ymbol")])
      str/lower-case
      keyword))

(defn ^:private forex?
  [body]
  (map2/get-in-by-substr body ["eta" "rom"]))

(defn invert-amounts
  [history]
  (->> history
       (mapv (fn [[timestamp amount]] [timestamp (with-precision 3 (/ 1M amount))]))))

(defn as-bucket
  [body]
  (let [currency? (forex? body)
        id        (get-symbol body currency?)
        post-fn   (if currency? invert-amounts vec)
        base-path (if currency? [:currency] [:stock])]
    {:bucket/id     id
     :bucket/path   (into base-path [id])
     :history/gross (-> body get-history post-fn)}))

(defn ^:private any->str
  [x]
  (-> x
      conversion/any->str
      str/upper-case))

(defn sanitize-replace-map
  [req-map api-key]
  (-> req-map
      (assoc :api-key api-key)
      (map/update-in-if [:from] any->str)
      (map/update-in-if [:to] any->str)
      (map/update-in-if [:symbol] any->str)))
