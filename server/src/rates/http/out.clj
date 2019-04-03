(ns rates.http.out
  (:require [quark.beta.http.protocols.http-client :as p.http]
            [rates.logic.core :as l]))

(defn ^:private currencies-one-day!
  [currencies base-currency millis http]
  (->> {:url     :currencies-one-day
        :replace (l/replace-map currencies base-currency millis)}
       (p.http/req! http)))

(defn currencies-overtime!
  [currencies base-currency now http]
  (->> (l/date-strs now)
       (map #(currencies-one-day! currencies base-currency % http))
       l/bodies->buckets))
