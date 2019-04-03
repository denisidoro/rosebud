(ns rosebud.playground
  (:require [rosebud.components.system :as components.system]))

;; Setup
(comment
  (def http (components.system/restart-and-get-component! :http))
  (components.system/restart-and-get-component! :http-impl)
  http
  (:bookmarks http))

;; Currency
(comment
  (http.out/currency-weekly :usd http))

;; Fundo
(comment
  (http.out/fundo-daily "17453850000148" http))

;; Stock
(comment
  (-> (http.out/stock-weekly :petr4.sa http)
      (update :history/gross (partial take 1))))

