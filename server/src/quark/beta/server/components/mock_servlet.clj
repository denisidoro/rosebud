(ns quark.beta.server.components.mock-servlet
  (:require [com.stuartsierra.component :as component]
            [io.pedestal.http :as bootstrap]))

(defrecord MockServlet [service]
  component/Lifecycle
  (start [this]
    (assoc this :instance (-> service :runnable-service bootstrap/create-server)))
  (stop  [this] (dissoc this :instance)))

(defn new-servlet [] (map->MockServlet {}))
