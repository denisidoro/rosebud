(ns rates.protocols.provider)

(defprotocol Provider
  (currencies-overtime! [component now currencies]))
