(ns rosebud.logic.grafana
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [quark.beta.time :as time]
            [quark.conversion.data :as conversion]
            [rosebud.logic.balance-map :as l.balance-map]))

(defn kw->wire
  [x]
  (if (keyword? x)
    (-> x str (subs 1))
    (str x)))

(defn wire->kw
  [x]
  (-> x
      conversion/any->str
      (str/replace "_" "-")
      keyword))

(defn any->wire
  [x]
  (if (keyword? x)
    (-> x str (subs 1))
    x))

(defn query-variable->coll
  [x]
  (map wire->kw
       (if (-> x first (= \())
         (-> x (subs 1 (-> x count dec)) (str/split #"\|"))
         [x])))

(defn any->typed
  [x]
  {:type :string
   :text x})

(defn target->props
  [target]
  (if (-> target first (= \{))
    (-> target
        (str/replace "\\" "")
        conversion/edn-str->edn)
    {:resolver (wire->kw target)}))

(defn curve->wire
  [balances]
  (->> balances
       (mapv (fn [[as-of amount]] [amount as-of]))
       (sort-by second)))

(defn curve-map->graph-data
  [curve-map]
  (->> curve-map
       l.balance-map/remove-empty
       (mapv (fn [[k curve]]
               {:target     (kw->wire k)
                :datapoints (curve->wire curve)}))))

(defn bucket-filter?
  [{:keys [key operator]}]
  (and (= key "bucket")
       (= operator "=")))

(defn adhoc-filters->buckets
  [adhoc-filters]
  (->> adhoc-filters
       (filter bucket-filter?)
       (map :value)
       (mapv #(-> % (str/replace "_" "/") keyword))))

(defn ^:private infer-type
  [col xs]
  (let [v    (first (map #(get % col) xs))
        num? (number? v)]
    (cond
      (and num? (> v 15000000)) :time
      num? :number
      :else :string)))

(defn ^:private build-column
  [col xs]
  {:text (conversion/any->str col)
   :type (infer-type col xs)})

(defn ^:private build-row
  [x cols]
  (mapv #(-> x (get %) any->wire) cols))

(def ^:private priority-columns
  [:id :label :name])

(defn ^:private prioritize-left
  [xs]
  (let [xs-set (set xs)]
    (into (vec (keep #(xs-set %) priority-columns))
          (->> (set/difference xs-set priority-columns) sort vec))))

(defn table
  ([xs] (table xs (->> xs (mapcat keys) set prioritize-left)))
  ([xs cols]
   {:type    :table
    :columns (mapv #(build-column % xs) cols)
    :rows    (mapv #(build-row % cols) xs)}))

(defn response->wire
  [{:keys [target datapoints] :as x}]
  (cond
    (and target datapoints) x
    :else (curve-map->graph-data x)))

(defn ^:private full-date-str->millis
  [date]
  (-> date
      (subs 0 10)
      time/date-str->millis))

(defn dates
  [req]
  [(-> req (get-in [:body-params :range :from]) full-date-str->millis)
   (-> req (get-in [:body-params :range :to]) full-date-str->millis)])
