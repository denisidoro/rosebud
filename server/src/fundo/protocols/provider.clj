(ns fundo.protocols.provider)

(defprotocol Provider
  (fundo-daily! [component cnpj]))
