(ns quark.beta.http.components.http
  (:require [clojure.string :as str]
            [com.stuartsierra.component :as component]
            [quark.beta.http.logic.serialization :as serialization]
            [quark.beta.http.protocols.http-client :as http-client]
            [quark.conversion.data :as conversion])
  (:import (java.io Writer)))

(defn ^:private interpolate-text
  [text replacements]
  (reduce (fn [u [k v]] (str/replace u (str k) (conversion/any->str v))) text replacements))

(defn ^:private sanitized-req-map
  [{:keys [url] :as req-map}
   bookmarks]
  (let [{:keys [replace] :as req-map'}
        (if (keyword? url)
          (merge (get bookmarks url)
                 (dissoc req-map :url))
          req-map)
        url' (interpolate-text (:url req-map') replace)]
    (-> req-map'
        (assoc :url url')
        (dissoc :replace))))

(defn- render-body
  "If we have a payload, use the serialize function from the request map to
   convert it to external"
  [{:keys [payload serialize] :as req-map}]
  (if payload (assoc req-map :body (serialize payload)) req-map))

(defn render-req [default-req-map req-map]
  (-> (merge default-req-map req-map)                       ; allow defaults to be overridden
      render-body))                                         ; serialize the request body

(defn- request-sync!
  "add the response details to the request map"
  [req-map http-impl bookmarks]
  (assoc req-map :response (http-client/req! http-impl (sanitized-req-map req-map bookmarks))))

(defn- parse-body
  "If we have a body, use the deserialize function from the request map to
   convert it back to internal"
  [{:keys [deserialize response] :as resp-map}]
  (cond
    (-> response :body nil?)
    resp-map

    (-> response :body string? not)
    (update-in resp-map [:response :body] (comp deserialize slurp))

    (:body response)
    (update-in resp-map [:response :body] deserialize)

    :else
    resp-map))

(defn handle-response [resp-map]
  (-> resp-map
      parse-body                                            ; parse the body and return the response
      :response                                             ; unwrap the response only from the resp-map
      (select-keys [:status :body :headers])))              ; drop excess http implementation keys

(defn with-bookmarks
  [this bookmarks]
  (update this :bookmarks merge bookmarks))

(defn do-req-resp! [request {:keys [http-impl bookmarks]}]
  (-> request
      (request-sync! http-impl bookmarks)
      handle-response))                                     ; parse and return the response

(defrecord Http [defaults bookmarks http-impl]
  ;; There are two arities of the req! protocol method to allow for more specific DSLs
  ;; Component starts with a default request map
  ;; This default map can be overridden on a per-request basis

  http-client/HttpClient
  (req! [this req-map]
    (http-client/req! this defaults req-map))
  (req! [this default-req-map req-map]
    (let [request (render-req default-req-map req-map)]
      (do-req-resp! request this)))

  component/Lifecycle
  (start [this] this)
  (stop [this] this)

  Object
  (toString [_] "<Http>"))

(defmethod print-method Http [_ ^Writer w]
  (.write w "<Http>"))

(def json-headers
  {"Content-Type"    "application/json; charset=utf-8"
   "Accept-Encoding" "gzip, deflate"})

(def html-headers
  (merge json-headers
         {"Content-Type" "text/html; charset=utf-8"}))

(def json-defaults
  {:method           :get
   :user-agent       "http-kit / your org"
   :headers          json-headers
   :serialize        serialization/write-json
   :deserialize      serialization/read-json
   :timeout          30000                                  ; 30 second timeout
   :keepalive        120000                                 ; 120 second keepalive
   :follow-redirects false
   :insecure?        true                                   ; TODO: FIX THIS
   :as               :text})

(def html-defaults
  (merge json-defaults
         {:headers   html-headers
          :serialize str}))

(defn new-http
  ([bookmarks] (new-http bookmarks json-defaults))
  ([bookmarks defaults] (map->Http {:defaults  defaults
                                    :bookmarks bookmarks})))
