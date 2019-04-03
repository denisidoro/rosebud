(ns quark.beta.http.protocols.http-client
  (:require [schema.core :as s]))

(defprotocol HttpClient
  "Protocol for making HTTP requests (outbound)"
  (req! [component req-map] [component defaults req-map] "Make a request, optionally overriding the default request map"))

(def IHttpClient (s/protocol HttpClient))
