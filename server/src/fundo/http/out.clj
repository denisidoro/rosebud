(ns fundo.http.out
  (:require [fundo.logic.core :as l]
            [quark.beta.http.protocols.http-client :as p.http]))

(defn fundo-daily!
  [cnpj http]
  (->> {:url     :fundo-daily
        :replace {:cnpj cnpj}}
       (p.http/req! http)
       :body
       (l/as-bucket cnpj)))

