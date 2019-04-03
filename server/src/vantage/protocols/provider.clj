(ns vantage.protocols.provider)

(defprotocol Provider
  (stock-weekly! [component symbol])
  (currency-weekly! [component currency]))
