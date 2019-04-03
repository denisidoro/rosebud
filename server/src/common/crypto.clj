(ns common.crypto
  (:require [quark.beta.crypto :as crypto]))

(def encrypt (partial crypto/offset 1))
(def decrypt (partial crypto/offset -1))
