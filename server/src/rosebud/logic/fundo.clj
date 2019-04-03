(ns rosebud.logic.fundo
  (:require [clojure.string :as str]
            [quark.collection.ns :as ns]
            [quark.collection.seq :as seq]))

(defn ^:private with-id
  [path id]
  (into (-> path drop-last vec) [(ns/unnamespaced id)]))

(defn replace-cnpj-by-id
  [investments fundo]
  (let [cnpj (-> fundo :bucket/id name (str/split #"\.") last)
        investment (seq/find-first #(-> % :fundo/cnpj (= cnpj)) investments)]
    (if investment
      (update fundo :bucket/path with-id (:bucket/id investment))
      fundo)))
