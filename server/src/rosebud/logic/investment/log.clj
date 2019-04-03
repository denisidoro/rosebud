(ns rosebud.logic.investment.log
  (:require [quark.beta.math.point :as point]
            [quark.beta.time :as time]
            [quark.collection.map :as map]
            [quark.collection.ns :as ns]
            [rosebud.logic.bucket :as l.bucket]))

(defn ^:private cleanup-path
  [path]
  (mapv ns/unnamespaced path))

(defn ^:private with-path
  [{:bucket/keys     [id]
    :investment/keys [wallet]
    :as              investment}
   hierarchy]
  (let [path (-> (l.bucket/find-path hierarchy wallet) (into [id]) cleanup-path)]
    (assoc investment :bucket/path path)))

(defn ^:private investment-positional->map
  [[id parent amount t0 tf final & _]]
  (map/assoc-if
   {:bucket/id                     id
    :investment/wallet             parent
    :investment/application-amount amount
    :investment/application-date   (time/date-str->millis t0)}
    :investment/maturity-date (some-> tf time/date-str->millis)
    :investment/maturity-amount final))

(defn ^:private cdi-investment-positional->map
  [[_ _ _ _ _ _ rate :as investment]]
  (-> investment
      investment-positional->map
      (assoc :cdi/rate (some-> rate (/ 100M)))))

(defn ^:private fundo-investment-positional->map
  [[_ _ _ _ _ _ fee cnpj :as investment]]
  (-> investment
      investment-positional->map
      (map/assoc-if :fundo/cnpj cnpj)
      (assoc :administration/fee (some-> fee (/ 100M)))))

(defn ^:private history-based-on-investment
  [{:investment/keys [application-amount application-date maturity-date maturity-amount]}]
  (cond-> [(point/new application-date application-amount)
           (point/new (time/millis->the-day-before application-date) 0M)]
    (and maturity-date maturity-amount)
    (concat [(point/new maturity-date maturity-amount)
             (point/new (time/millis->the-day-after maturity-date) 0M)])))

(defn ^:private with-gross-history
  [investment balance-history]
  (l.bucket/with-gross-history
    investment
    (history-based-on-investment investment)
    balance-history))

(defmulti internalize
  (fn [_ kind] kind))

(defmethod internalize :fundo
  [investment _]
  (fundo-investment-positional->map investment))

(defmethod internalize :default
  [investment _]
  (cdi-investment-positional->map investment))

(defn ^:private risk
  [kind]
  (if (#{:coe :fundo} kind)
    :high
    :low))

(defn ^:private lifecycle
  [{:investment/keys [maturity-date]}
   now]
  (if (and maturity-date (> now maturity-date))
    :mature
    :yielding))

(defn ^:private with-default-tags
  [investment kind now]
  (update investment :bucket/tags merge {:kind      kind
                                         :risk      (risk kind)
                                         :lifecycle (lifecycle investment now)}))

(defn ^:private investment->map
  [investment
   {:wallet/keys  [hierarchy]
    :balance/keys [history]}
   kind
   now]
  (-> investment
      (internalize kind)
      (with-default-tags kind now)
      (with-path hierarchy)
      (with-gross-history history)))

(defn from-log
  [log now]
  (->> log
       (map/filter-keys #(-> % namespace (= "investment")))
       (map/map-keys #(-> % name keyword))
       (mapcat (fn [[kind investments]] (mapv #(investment->map % log kind now) investments)))))
