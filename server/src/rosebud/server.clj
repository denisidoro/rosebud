(ns rosebud.server
  (:gen-class)
  (:require [rosebud.components.system :as components.system]))

(defn run-dev
  "The entry-point for 'lein run-dev'"
  [& args]
  (println "\nStarting [DEV] server...")
  (components.system/create-and-start-system! :dev)
  (println "\n[DEV] server created!"))

(defn restart
  "The entry-point for 'lein run-dev'"
  [& args]
  (println "\nRestarting [DEV] server...")
  (components.system/stop-system!)
  (components.system/create-and-start-system! :dev)
  (println "\n[DEV] server restarted!"))

(defn -main
  "The entry-point for 'lein run'"
  [& args]
  (println "\nStarting server...")
  (components.system/create-and-start-system! :base)
  (println "\nServer started!"))

