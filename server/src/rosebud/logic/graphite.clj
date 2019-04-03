(ns rosebud.logic.graphite
  (:require [clojure.string :as str]))

(defn kw->str
  [kw]
  (-> kw
      str
      (subs 1)
      (str/replace #"[/\.]" "_")))

(defn any->str
  [x]
  (if (keyword? x)
    (kw->str x)
    x))

(defn kv->str
  [[k v]]
  (str (any->str k) "=" (any->str v)))

(defn one-line
  ([path amount timestamp]
   (one-line path amount timestamp {}))
  ([path amount timestamp tags]
   (str (str/join ";" (into [path] (map kv->str tags)))
        " "
        (->> amount bigdec (format "%.2f"))
        " "
        (int (/ timestamp 1000)))))

;; TODO: use interpolated value
(defn bucket->msg
  [{:bucket/keys  [path tags]
    :history/keys [interpolated-gross gross]}]
  (when (and path gross)
    (let [path-str (->> path (map any->str) (str/join "."))
          lines    (->> (or interpolated-gross gross)
                        (map (fn [[timestamp amount]] (one-line path-str amount timestamp tags)))
                        (str/join "\n"))]
      (str lines "\n"))))

(defn msg
  [buckets]
  (->> buckets
       (mapv bucket->msg)
       (keep identity)
       (apply str)))
