(ns rosebud.handlers.grafana
  (:refer-clojure :exclude [resolve])
  (:require [ring.util.response :as ring.resp]
            [rosebud.logic.grafana :as l.grafana]
            [rosebud.resolvers :as res]))

(def ^:private respond
  ring.resp/response)

(defn annotations
  [_]
  (-> []
      respond))

(defn tag-keys
  [_]
  (->> res/tags
       keys
       (mapv l.grafana/any->typed)
       respond))

(defn ^:private find-values
  [req]
  (let [{:keys [target key]} (:body-params req)
        k        (-> target (or key) l.grafana/wire->kw)
        resolver (get res/tags k)
        values   (if resolver
                   (resolver req)
                   (keys res/queries))]
    (->> values
         (mapv l.grafana/kw->wire))))

(defn tag-values
  [req]
  (->> req
       find-values
       (mapv l.grafana/any->typed)
       respond))

(defn ^:private get-resolver!
  [k target]
  (or (res/queries k)
      (let [m {:k k :target target :available (keys res/queries)}]
        (throw (ex-info (str "Invalid resolver " m) m)))))

(defn ^:private resolve!
  [req target table?]
  (let [{:keys [resolver] :as props} (l.grafana/target->props target)
        resolver-fn (get-resolver! resolver target)
        req'        (update req :body-params merge props)
        result      (resolver-fn req')
        post-fn     l.grafana/response->wire]
    (cond
      table? (l.grafana/table result)
      (vector? result) (mapv post-fn result)
      :else [(post-fn result)])))

(defn query
  [{{:keys [targets]} :body-params
    :as               req}]
  (->> targets
       (mapv (fn [{:keys [target type]}] (resolve! req target (= type "table"))))
       flatten
       respond))

(defn search
  [req]
  (->> req
       find-values
       respond))
