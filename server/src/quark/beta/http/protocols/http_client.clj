(ns quark.beta.http.protocols.http-client)

(defprotocol HttpClient
  "Protocol for making HTTP requests (outbound)"
  (req! [component req-map] [component defaults req-map] "Make a request, optionally overriding the default request map"))
