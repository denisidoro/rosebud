(ns quark.beta.server.components.config
  (:require [clojure.java.io :as io]
            [clojure.tools.reader.edn :as edn]
            [com.stuartsierra.component :as component]
            [quark.beta.server.protocols.config :as p.config])
  (:import (java.io Writer)))

(defn ^:private read-config
  [config-file]
  (try
    (->> config-file
         io/resource
         slurp
         edn/read-string)
    (catch Exception _
      {})))

(defn ^:private start-component
  [{:keys [config-file] :as this}
   base-config-map]
  (assoc this
         :config (merge base-config-map
                        (read-config config-file))))

(defrecord Config [config-file base-config-map]

  p.config/Config
  (get! [this config-path]
    (if-some [val (p.config/get-optional this config-path)]
      val
      (throw (ex-info "Missing Config"
                      {:type    ::missing-config
                       :details {:from ::get! :config-path config-path}}))))

  (get-optional [this config-path]
    (get-in (:config this) config-path))

  (get-env-var [_ name fallback]
    (or (System/getenv name)
        fallback))

  component/Lifecycle
  (start [this]
    (if-not (:config this)
      (start-component this base-config-map)
      this))
  (stop [this] (dissoc this :config))

  Object
  (toString [_] "<Config>"))

(defmethod print-method Config [_ ^Writer w]
  (.write w "<Config>"))

(defn new-config
  [base-config-map]
  (map->Config {:config-file     "config.edn"
                :base-config-map base-config-map}))
