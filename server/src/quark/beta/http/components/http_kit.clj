(ns quark.beta.http.components.http-kit
  (:require [com.stuartsierra.component :as component]
            [org.httpkit.client :as client]
            [quark.beta.http.protocols.http-client :as pro.http])
  (:import (java.io Writer)
           (java.net URI)
           (javax.net.ssl SNIHostName SSLEngine SSLParameters)))

(def http-kit-keys
  [:url :method :body :oauth-token :user-agent :headers :form-params
   :query-params :keepalive :timeout :filter :multipart :max-redirects
   :follow-redirects :insecure? :client])

(defn sni-configure
  [^SSLEngine ssl-engine ^URI uri]
  (let [^SSLParameters ssl-params (.getSSLParameters ssl-engine)]
    (.setServerNames ssl-params [(SNIHostName. (.getHost uri))])
    (.setSSLParameters ssl-engine ssl-params)))

(defn new-client
  []
  (client/make-client {:ssl-configurer sni-configure}))

(defrecord HttpKit []
  pro.http/HttpClient
  (req! [defaults {:keys [url method] :as req-map}]
    ;; Use only the keys that http-kit understands
    (let [valid-http-kit-req (select-keys (merge defaults req-map) http-kit-keys)
          response           @(client/request valid-http-kit-req identity)]
      (when (:error response)
        (throw (ex-info "Http error"
                        {:from         ::req!
                         :reason       :out-response-exception
                         :url          url
                         :method       method
                         :cause        (:error response)})))
      response))

  component/Lifecycle
  (start [this] (if (:client this) this (assoc this :client (new-client))))
  (stop  [this] (dissoc this :client))

  Object
  (toString [_] "<HttpKit>"))

(defmethod print-method HttpKit [_ ^Writer w]
  (.write w "<HttpKit>"))

(defn new-http-client []
  (map->HttpKit {}))
