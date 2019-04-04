(ns rosebud.components.bucket.components.provider
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [com.stuartsierra.component :as component]
            [fundo.protocols.provider :as p.fundo]
            [quark.beta.time :as time]
            [rosebud.components.bucket.protocols.provider :as p]
            [rosebud.logic.fundo :as l.fundo]
            [rosebud.logic.investment.core :as l.investment]
            [rosebud.logic.wallet :as l.wallet]
            [vantage.protocols.provider :as p.vantage])
  (:import (java.io Writer)))

(def ^:private ^:const ^String component-name
  "<BucketProvider>")

(defn ^:private read-log
  [path]
  (try
    (println (str "Attempting to read " path "..."))
    (-> path io/resource slurp read-string)
    (catch Exception _
      (println (str "Unable to read " path "!"))
      nil)))

;; TODO: use a component
(defn ^:private get-log
  []
  (or (read-log "log.edn")
      (read-log "example_log.edn")))

(defn ^:private error-message
  [exception & elems]
  (str (str/join " " (concat ["Failed to fetch"] elems))
       "\n"
       (str exception)
       "\n"
       (.getMessage exception)))

(defmacro ^:private safe-call
  [group & body]
  `(try
     ~@body
     (catch Exception e#
       (println (error-message e# ~group))
       [])))

(defn ^:private try-f
  [f group elem]
  (try
    (f elem)
    (catch Exception e#
      (println (error-message e# group elem))
      nil)))

(defmacro ^:private map-safe-call
  [group f coll]
  `(->> ~coll
        (map (partial try-f ~f ~group))
        (filter identity)))

(defn ^:private start!
  [fundo vantage]
  (let [log         (get-log)
        now         (time/now-millis)
        investments (safe-call
                      "investments"
                      (l.investment/from-log log now))
        wallets     (safe-call
                      "wallets"
                      (l.wallet/from-log log))
        fundo-invs  (filter :fundo/cnpj investments)
        fundos      (->> fundo-invs
                         (map :fundo/cnpj)
                         (map-safe-call "fundo" #(p.fundo/fundo-daily! fundo %))
                         (map #(l.fundo/replace-cnpj-by-id fundo-invs %)))
        currencies  (->> log
                         :currency/track
                         (map-safe-call "currency" #(p.vantage/currency-weekly! vantage %)))
        stocks      (->> log
                         :stock/track
                         (map-safe-call "stock" #(p.vantage/stock-weekly! vantage %)))]
    {:investments investments
     :wallets     wallets
     :fundos      fundos
     :currencies  currencies
     :stocks      stocks}))

(defrecord Provider [fundo vantage]

  p/Provider
  (get-fundos [this] (:fundos this))
  (get-wallets [this] (:wallets this))
  (get-investments [this] (:investments this))
  (get-stocks [this] (:stocks this))
  (get-currencies [this] (:currencies this))

  component/Lifecycle
  (start [this]
    (merge this (start! fundo vantage)))

  (stop [this]
    (dissoc this :investments :fundos :currencies :stocks :wallets :fundo :vantage))

  Object
  (toString [_]
    component-name))

(defmethod print-method Provider
  [_ ^Writer w]
  (.write w component-name))

(defn new-provider
  []
  (map->Provider {}))
