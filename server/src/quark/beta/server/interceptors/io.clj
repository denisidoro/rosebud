(ns quark.beta.server.interceptors.io
  (:require [io.pedestal.http.content-negotiation :as content-negotiation]
            [io.pedestal.interceptor :as i]
            [quark.conversion.data :as conversion]))

(def ^:private supported-types
  ["text/html" "application/edn" "application/json" "text/plain"])

(def negotiate-content
  (partial content-negotiation/negotiate-content supported-types))

(defn ^:private accepted-type
  [context]
  (get-in context [:request :accept :field] "text/plain"))

(defn ^:private transform-content
  [body content-type]
  (case content-type
    "text/html" body
    "text/plain" body
    "application/edn" (pr-str body)
    "application/json" (conversion/edn->json body)))

(defn ^:private coerce-to
  [response content-type]
  (-> response
      (update :body transform-content content-type)
      (assoc-in [:headers "Content-Type"] content-type)))

(def coerce-body
  {:name ::coerce-body
   :leave
   (fn [context]
     (cond-> context
       (nil? (get-in context [:response :headers "Content-Type"]))
       (update-in [:response] coerce-to (accepted-type context))))})

(defn merge-body-params
  []
  (i/interceptor
   {:name  ::db
    :enter (fn [{{:keys [edn-params json-params]} :request :as context}]
             (-> context
                 (assoc-in [:request :body-params] (or edn-params json-params))))}))
