(ns rosebud.components.bucket.protocols.provider)

(defprotocol Provider
  (get-investments [component])
  (get-fundos [component])
  (get-wallets [component])
  (get-stocks [component])
  (get-currencies [component]))
