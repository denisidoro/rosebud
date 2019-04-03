(ns rosebud.handlers.dev
  (:require [fundo.protocols.provider :as p.fundo]
            [ring.util.response :as ring.resp]
            [vantage.protocols.provider :as p.vantage]))

(def respond ring.resp/response)

(defn stock
  [{{:keys [vantage]} :components}]
  (-> (p.vantage/stock-weekly! vantage :petr4.sa)
      respond))

(defn currency
  [{{:keys [vantage]} :components}]
  (-> (p.vantage/currency-weekly! vantage :usd)
      respond))

(defn fundo
  [{{:keys [fundo]} :components}]
  (-> (p.fundo/fundo-daily! fundo "17453850000148")
      respond))
