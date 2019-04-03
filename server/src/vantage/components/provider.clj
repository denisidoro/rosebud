(ns vantage.components.provider
  (:require [com.stuartsierra.component :as component]
            [common.config :as config]
            [quark.beta.http.components.http :as http]
            [quark.beta.server.protocols.config :as p.config]
            [vantage.http.config :as http.config]
            [vantage.http.out :as http.out]
            [vantage.protocols.provider :as p])
  (:import (java.io Writer)))

(def ^:private ^:const ^String component-name
  "<VantageProvider>")

(defrecord Provider [config http]

  p/Provider
  (stock-weekly!
    [{:keys [api/key]} symbol]
    (http.out/stock-weekly! symbol key http))

  (currency-weekly!
    [{:keys [api/key currency/base]} currency]
    (http.out/currency-weekly! currency base key http))

  component/Lifecycle
  (start [this]
    (-> this
        (assoc :api/key (p.config/get-env-var config "RB_VANTAGE_API_KEY" config/default-vantage-api-key))
        (assoc :currency/base (p.config/get-env-var config "RB_BASE_CURRENCY" config/default-base-currency))
        (update :http http/with-bookmarks http.config/bookmarks)))

  (stop [this]
    (dissoc this :api/key :currency/base :http))

  Object
  (toString [_]
    component-name))

(defmethod print-method Provider
  [_ ^Writer w]
  (.write w component-name))

(defn new-provider
  []
  (map->Provider {}))
