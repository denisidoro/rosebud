(ns fundo.http.config
  (:require [common.crypto :as crypto]))

(def bookmarks
  {:fundo-daily
   {:url    (str "https://" (crypto/decrypt "bttfut.dpnqbsbdbpefgvoept") ".s3-sa-east-1.amazonaws.com/cvm/:cnpj")
    :method :get}})
