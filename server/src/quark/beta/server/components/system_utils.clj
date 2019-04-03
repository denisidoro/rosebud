(ns quark.beta.server.components.system-utils
  (:require [com.stuartsierra.component :as component])
  (:import [clojure.lang ExceptionInfo]))

(def system (atom nil))

(defn- quiet-start [system]
  (try
    (component/start system)
    (catch ExceptionInfo ex
      (throw (or (.getCause ex) ex)))))

(defn start-system! []
  (swap! system quiet-start))

(defn get-component [component-name]
  (some-> system deref (get component-name)))

(defn get-component! [c]
  (or (get-component c)
      (throw (ex-info "Component not found"
                      {:from      ::get-component!
                       :component c
                       :reason    "Unknown component"}))))

(defn stop-components! []
  (component/stop system)
  (reset! system nil))

(defn clear-components! []
  (reset! system nil))

(defn stop-system! []
  (stop-components!)
  (shutdown-agents))

(defn ^:private system-for-env [environment systems]
  (get systems environment (:base-system systems)))

(defn bootstrap! [systems-map environment]
  (let [system-map ((system-for-env environment systems-map))]
    (->> system-map
         component/start
         (reset! system))))
