(ns quark.beta.server.components.debug-logger
  "Persists the last interceptor error for debugging purposes. Should only be
  used in test environments."
  (:require [com.stuartsierra.component :as component]
            [quark.beta.server.protocols.debug-logger :as protocols.debug-logger])
  (:import (java.io Writer)))

(def ^:dynamic *latest-error* nil)

(defrecord DebugLogger [config]

  component/Lifecycle
  (start [this] this)
  (stop  [this] this)

  protocols.debug-logger/DebugLogger
  (register-last-error! [_ error]
    (set! *latest-error* error))
  (get-last-error [_]
    *latest-error*)

  Object
  (toString [_] "<DebugLogger>"))

(defmethod print-method DebugLogger [_ ^Writer w]
  (.write w "<DebugLogger>"))

(defn new-debug-logger []
  (map->DebugLogger {}))
