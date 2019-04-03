(ns quark.beta.server.protocols.debug-logger
  "Store and retrieve the last error for debugging purposes.")

(defprotocol DebugLogger
  (register-last-error! [this error] "Store last error for debugging purposes")
  (get-last-error       [this]       "Retrieve last error for debugging purposes"))

(def IDebugLogger (:on-interface DebugLogger))
