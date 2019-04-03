(ns rosebud.logic.wallet
  (:require [quark.navigation.core :as nav]
            [rosebud.logic.bucket :as l.bucket]))

(defn ^:private with-path
  [{:bucket/keys [id]
    :as          wallet}
   hierarchy]
  (let [path (l.bucket/find-path hierarchy id)]
    (assoc wallet :bucket/path path)))

(defn ^:private with-gross-history
  [wallet balance-history]
  (l.bucket/with-gross-history
    wallet
    []
    balance-history))

(defn ^:private with-default-tags
  [wallet]
  (update wallet :bucket/tags merge {:kind :wallet
                                     :risk :low}))

(defn ^:private bucket-id->bucket
  [bucket-id
   {:wallet/keys  [hierarchy]
    :balance/keys [history]}]
  (-> {:bucket/id bucket-id}
      with-default-tags
      (with-path hierarchy)
      (with-gross-history history)))

(defn ^:private ids
  [hierarchy]
  (->> hierarchy
       nav/path-seq
       (reduce (fn [acc {:keys [path form]}] (concat acc path [form])) #{})
       (filter keyword?)
       set))

(defn from-log
  [{:wallet/keys [hierarchy] :as log}]
  (->> hierarchy
       ids
       (map #(bucket-id->bucket % log))
       (filter #(-> % :history/gross seq))))

(defn wallet?
  [x]
  (some-> x :bucket/tags :kind (= :wallet)))
