(ns rates.http.config
  (:require [common.crypto :as crypto]))

(def bookmarks
  {:currencies-one-day
   {:url    (str "https://" (crypto/decrypt "bqj/pqfosbuft/jp") "/:date?base=:base&symbols=:symbols")
    :method :get}})
