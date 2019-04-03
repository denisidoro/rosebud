(ns quark.beta.server.interceptors.error-info
  (:require [io.pedestal.interceptor.error :as interceptor.error]
            [quark.beta.server.protocols.debug-logger :as protocols.debug-logger]))

(defn- register-error-for-debugging [context error]
  (some-> context :request :components :debug-logger
          (protocols.debug-logger/register-last-error! error)))

(def log-error-during-debugging
  ;; hacky way to grab an exception as it is being propagated upwards and
  ;; register it for debug logging. Eventually find a more basic way to do
  ;; this.
  (interceptor.error/error-dispatch [ctx ex]
                                    :else
                                    (do
                                      (register-error-for-debugging ctx ex)
                                      (assoc ctx :io.pedestal.interceptor.chain/error ex))))

