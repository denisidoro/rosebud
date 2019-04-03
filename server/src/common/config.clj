(ns common.config
  (:require [common.crypto :as crypto]))

(def ^:const default-base-currency "BRL")
(def ^:const default-vantage-api-key (crypto/decrypt "9ZO6IC8WTOHCZYC["))
