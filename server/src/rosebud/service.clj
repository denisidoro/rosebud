(ns rosebud.service
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.body-params :as body-params]
            [quark.beta.server.interceptors.error-info :as int.error]
            [quark.beta.server.interceptors.io :as int.io]
            [rosebud.handlers.core :as h.core]
            [rosebud.handlers.dev :as h.dev]
            [rosebud.handlers.grafana :as h.grafana]
            [rosebud.handlers.graphite :as h.graphite]))

(def routes
  `{"/" {:interceptors  [(body-params/body-params)
                         (int.io/merge-body-params)
                         (int.io/negotiate-content)
                         int.io/coerce-body
                         http/html-body
                         int.error/log-error-during-debugging]
         :get           h.core/home-page
         "/search"      {:post h.grafana/search}
         "/query"       {:post h.grafana/query}
         "/annotations" {:post h.grafana/annotations}
         "/tag-keys"    {:post h.grafana/tag-keys}
         "/tag-values"  {:post h.grafana/tag-values}
         "/graphite"    {:post h.graphite/plain-text-dump}
         "/dev"         {"/stock"    {:get h.dev/stock}
                         "/currency" {:get h.dev/currency}
                         "/fundo"    {:get h.dev/fundo}}}})
