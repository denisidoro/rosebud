(ns fundo.components.provider
  (:require [com.stuartsierra.component :as component]
            [fundo.http.config :as http.config]
            [fundo.http.out :as http.out]
            [fundo.protocols.provider :as p]
            [quark.beta.http.components.http :as http])
  (:import (java.io Writer)))

(def ^:private ^:const ^String component-name
  "<FundoProvider>")

(defrecord Provider [http]

  p/Provider
  (fundo-daily!
    [_ cnpj]
    (http.out/fundo-daily! cnpj http))

  component/Lifecycle
  (start [this]
    (update this :http http/with-bookmarks http.config/bookmarks))

  (stop [this]
    (dissoc this :http))

  Object
  (toString [_]
    component-name))

(defmethod print-method Provider
  [_ ^Writer w]
  (.write w component-name))

(defn new-provider
  []
  (map->Provider {}))
