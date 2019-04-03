(ns rosebud.handlers.core
  (:require [ring.util.response :as ring.resp]))

(def respond ring.resp/response)

(defn home-page
  [_]
  (respond "Hello <b>world</b>!"))
