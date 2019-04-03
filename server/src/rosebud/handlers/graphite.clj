(ns rosebud.handlers.graphite
  (:require [quark.beta.time :as time]
            [ring.util.response :as ring.resp]
            [rosebud.components.bucket.protocols.provider :as p.bucket]
            [rosebud.logic.bucket :as l.bucket]
            [rosebud.logic.graphite :as l.graphite]))

(def respond ring.resp/response)

(defn plain-text-dump
  [{{:keys [bucket]} :components}]
  (let [investments   (p.bucket/get-investments bucket)
        wallets       (p.bucket/get-wallets bucket)
        stocks        (p.bucket/get-stocks bucket)
        currencies    (p.bucket/get-currencies bucket)
        fundos        (p.bucket/get-fundos bucket)
        now           (time/now-millis)
        all-millis    (l.bucket/find-all-millis+end-of-months now (concat investments wallets))
        investments+  (map #(l.bucket/with-interpolated-gross-history all-millis %) investments)
        wallets+      (map #(l.bucket/with-interpolated-gross-history all-millis %) wallets)
        buckets       (concat investments+ wallets+ stocks currencies fundos)]
    (-> buckets
        vec
        l.graphite/msg
        respond)))
