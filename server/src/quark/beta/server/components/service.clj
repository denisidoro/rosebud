(ns quark.beta.server.components.service
  (:require [com.stuartsierra.component :as component]
            [io.pedestal.http :as bootstrap]
            [io.pedestal.http.route :as route]
            [io.pedestal.interceptor.helpers :refer [before]]
            [quark.beta.server.protocols.config :as p.config])
  (:import (java.io Writer)))

(defn- add-system [service]
  (before (fn [context] (assoc-in context [:request :components] service))))

(defn system-interceptors
  "Extend to service's interceptors to include one to inject the components
   into the request object"
  [service-map service]
  (update-in service-map
             [::bootstrap/interceptors]
             #(vec (->> % (cons (add-system service))))))

(defn base-service [routes port]
  {:env                        :prod
   ::bootstrap/router          :prefix-tree
   ::bootstrap/routes          #(route/expand-routes (deref routes))
   ::bootstrap/resource-path   "/public"
   ::bootstrap/type            :jetty
   ::bootstrap/port            port
   ::bootstrap/host            "0.0.0.0"                    ; TODO: fix this!!!
   ::bootstrap/allowed-origins {:allowed-origins (constantly true)}}) ;; TODO: WARNING!!! FIX THIS

(defn prod-init [service-map]
  (bootstrap/default-interceptors service-map))

(defn dev-init [service-map]
  (-> service-map
      (merge {:env                        :dev
              ;; do not block thread that starts web server
              ::bootstrap/join?           false
              ;; Content Security Policy (CSP) is mostly turned off in dev mode
              ::bootstrap/secure-headers {:content-security-policy-settings {:object-src "none"}}
              ;; all origins are allowed in dev mode
              ::bootstrap/allowed-origins {:creds true :allowed-origins (constantly true)}})
      ;; Wire up interceptor chains
      bootstrap/default-interceptors
      bootstrap/dev-interceptors))

(defn runnable-service [config routes service]
  (let [env          (p.config/get! config [:server/env])
        port         (p.config/get! config [:server/port])
        service-conf (base-service routes port)]
    (-> (if (= :prod env)
          (prod-init service-conf)
          (dev-init service-conf))
        (system-interceptors service))))

(defrecord Service [config routes]
  component/Lifecycle
  (start [this]
    (assoc this
           :runnable-service
           (runnable-service config (:routes routes) this)))

  (stop [this]
    (dissoc this :runnable-service))

  Object
  (toString [_] "<Service>"))

(defmethod print-method Service [v ^Writer w]
  (.write w "<Service>"))

(defn new-service [] (map->Service {}))
