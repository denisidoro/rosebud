(ns quark.beta.server.protocols.config
  "Store and retrieve the last error for debugging purposes.")

(defprotocol Config
  "Runtime configuration"
  (get! [component config-path] "Get an item based on a [:path :to :item], or raise an exception if not found.")
  (get-env-var [component name fallback])
  (get-optional [component config-path] "Get an optional item based on a [:path :to :item]. Returns nil if not found"))
