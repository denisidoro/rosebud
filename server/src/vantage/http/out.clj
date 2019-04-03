(ns vantage.http.out
  (:require [quark.beta.http.protocols.http-client :as p.http]
            [vantage.logic.core :as l]))

(defn stock-weekly!
  [symbol api-key http]
  (->> {:url     :stock-weekly
        :replace (l/sanitize-replace-map
                  {:symbol symbol}
                  api-key)}
       (p.http/req! http)
       :body
       l/as-bucket))

(defn currency-weekly!
  [currency base-currency api-key http]
  (->> {:url     :currency-weekly
        :replace (l/sanitize-replace-map
                  {:from base-currency
                   :to   currency}
                  api-key)}
       (p.http/req! http)
       :body
       l/as-bucket))

