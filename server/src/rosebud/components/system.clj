(ns rosebud.components.system
  (:refer-clojure :exclude [test])
  (:require [com.stuartsierra.component :as component]
            [fundo.components.provider :as fundo]
            [quark.beta.http.components.http :as http]
            [quark.beta.http.components.http-kit :as http.kit]
            [quark.beta.http.components.mock-http :as http.mock]
            [quark.beta.server.components.config :as config]
            [quark.beta.server.components.debug-logger :as logger.debug]
            [quark.beta.server.components.dev-servlet :as servlet.dev]
            [quark.beta.server.components.mock-servlet :as servlet.mock]
            [quark.beta.server.components.routes :as routes]
            [quark.beta.server.components.service :as service]
            [quark.beta.server.components.system-utils :as system-utils]
            [rates.components.provider :as rates]
            [rosebud.components.bucket.components.provider :as bucket]
            [rosebud.service]
            [vantage.components.provider :as vantage]))

(def base-config-map
  {:server/env  :prod
   :server/port 8080})

(def local-config-map
  (merge base-config-map
         {:server/env :dev}))

(def web-app-deps
  [:config :routes :http :vantage :fundo :rates :bucket])

(defn base []
  (component/system-map
   :config (config/new-config base-config-map)
   :http-impl (http.kit/new-http-client)
   :http (component/using (http/new-http {}) [:http-impl])
   :vantage (component/using (vantage/new-provider) [:http :config])
   :fundo (component/using (fundo/new-provider) [:http])
   :rates (component/using (rates/new-provider) [:http :config])
   :bucket (component/using (bucket/new-provider) [:fundo :vantage])
   :routes (routes/new-routes #'rosebud.service/routes)
   :service (component/using (service/new-service) web-app-deps)
   :servlet (component/using (servlet.dev/new-servlet) [:service])))

(defn test []
  (merge
   (base)
   (component/system-map
    :config (config/new-config local-config-map)
    :servlet (component/using (servlet.mock/new-servlet) [:service])
    :debug-logger (logger.debug/new-debug-logger)
    :http (component/using (http.mock/new-mock-http) [:config])
    :service (component/using (service/new-service) (conj web-app-deps :debug-logger)))))

(def systems-map
  {:dev  base
   :test test
   :base base})

(defn create-and-start-system!
  [env]
  (system-utils/bootstrap! systems-map env))

(defn ensure-system-up!
  ([] (ensure-system-up! :base))
  ([env]
   (or @system-utils/system
       (create-and-start-system! env))))

(defn stop-system! []
  (or @system-utils/system
      (system-utils/stop-components!)))

(defn restart-and-get-component!
  [component-name]
  (stop-system!)
  (ensure-system-up!)
  (system-utils/get-component! component-name))
