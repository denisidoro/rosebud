(ns quark.beta.http.components.mock-http
  (:require [com.stuartsierra.component :as component]
            [quark.beta.http.components.http :as h-com]
            [quark.beta.http.protocols.http-client :as pro.http]))

(def ^:dynamic *response* {})
(def ^:dynamic *responses* nil)

(defn- current-responses [responses] (merge @responses *responses*))

(defn- respond-with [reqmap response]
  (cond (instance? Exception response)
        (throw response)

        (fn? response)
        (response reqmap)

        :else
        response))

(defn- lookup-key [{:keys [local remote url]}]
  (or local remote url))

(defn- response-key->pred [response-key]
  (if (fn? response-key)
    response-key
    (comp #{response-key} lookup-key)))

(defn- find-response [{:keys [url] :as unrendered} responses]
  ;; Unsupported: multiple requests to one URI expected to have different
  ;; responses We render a full body with the exception to ensure it is visible
  ;; within selvage test failure output
  (let [candidates (map  (fn [[k resp]] [(response-key->pred k) resp]) responses)
        resp       (some (fn [[k-pred resp]] (when (k-pred unrendered) resp)) candidates)]
    (or resp
        (if responses
          (when (not= {} *response*)
            *response*)
          *response*)
        (do (prn (str "*responses* is non-nil, but uri `" url "` was not found in the map"))
            {:status 500
             :body {:error "*responses* is non-nil, but uri was not found in the map"
                    :url   url}}))))

(defn update-atom! [{:keys [requests responses]} unrendered rendered]
  (let [lookup-key (lookup-key unrendered)]
    (swap! requests update-in [lookup-key] #(conj (or % []) rendered))
    (let [raw-response (find-response unrendered (current-responses responses))]
      (respond-with unrendered raw-response))))

(defrecord Http [defaults responses requests]
  pro.http/HttpClient
  (req! [this req-map]
    (pro.http/req! this defaults req-map))
  (req! [this default-req-map req-map]
    (update-atom! this req-map (h-com/render-req default-req-map req-map)))

  component/Lifecycle
  (start [this] this)
  (stop  [this] this))

(defn new-mock-http
  ([]         (new-mock-http h-com/json-defaults))
  ([defaults] (map->Http {:defaults  defaults
                          :responses (atom nil)
                          :requests  (atom {})})))

(defn get-requests [component lookup-key]
  (get (-> component :requests deref) lookup-key))

(defn clear-requests!
  ([component]
   (-> component :requests (reset! {}))
   (reset! (:responses component) nil))
  ([component lookup-key]
   (-> component :requests (swap! assoc lookup-key []))
   (swap! (:responses component) dissoc lookup-key)))

(defmacro with-response [response & forms]
  ;; Simple macro to bind a single response for a single HTTP out request
  ;; Does not require specifying the URI that is expected to be called
  ;; Response is meant to be just the value of the body
  `(binding [*response* ~response] (do ~@forms)))

(defn get-responses [component]
  @(:responses component))

(defmacro with-responses [responses & forms]
  ;; More powerful macro to bind a map of {"uri" {:response "value"}}
  ;; Useful for multiple outbound calls to different URIs with different response expectations
  ;; Each response in the map is meant to be just the value of the body
  `(binding [*responses* (merge *responses* ~responses)] (do ~@forms)))

(defn last-call [component lookup-key]
  ;; Note: using this function after triggering something interferes with midje's provided
  (last (get-requests component lookup-key)))
