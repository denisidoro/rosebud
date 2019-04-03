(ns vantage.http.config
  (:require [common.crypto :as crypto]))

(def bookmarks
  {:stock-weekly
   {:url    (str "https://www." (crypto/decrypt "bmqibwboubhf/dp") "/query?function=TIME_SERIES_WEEKLY&symbol=:symbol&apikey=:apikey&datatype=json")
    :method :get}

   :currency-weekly
   {:url    (str "https://www." (crypto/decrypt "bmqibwboubhf/dp") "/query?function=FX_WEEKLY&from_symbol=:from&to_symbol=:to&apikey=:apikey&datatype=json")
    :method :get}})
