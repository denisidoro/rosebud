(ns rates.components.provider
  (:require [com.stuartsierra.component :as component]
            [common.config :as config]
            [quark.beta.http.components.http :as http]
            [quark.beta.server.protocols.config :as p.config]
            [rates.http.config :as http.config]
            [rates.http.out :as http.out]
            [rates.protocols.provider :as p])
  (:import (java.io Writer)))

(def ^:private ^:const ^String component-name
  "<RatesProvider>")

(defrecord Provider [config http]

  p/Provider
  (currencies-overtime!
    [{:keys [currency/base]} now currencies]
    (http.out/currencies-overtime! currencies base now http))

  component/Lifecycle
  (start [this]
    (-> this
        (assoc :currency/base (p.config/get-env-var config "RB_BASE_CURRENCY" config/default-base-currency))
        (update :http http/with-bookmarks http.config/bookmarks)))

  (stop [this]
    (dissoc this :currency/base :http))

  Object
  (toString [_]
    component-name))

(defmethod print-method Provider
  [_ ^Writer w]
  (.write w component-name))

(defn new-provider
  []
  (map->Provider {}))
