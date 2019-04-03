(ns quark.beta.http.logic.serialization
  (:require [cheshire.core :as json]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.walk :as walk]))

(defn ^:private replace-char
  ;; Replaces the from character with the to character in s, which can be a String or a Keyword
  ;; Does nothing if s is a keyword that is in the exception set
  [s from to exceptions]
  (if (contains? exceptions s)
    s
    (keyword (str/replace (name s) from to))))

(def underscore->dash-exceptions #{})

(defn ^:private replace-char-gen
  ;; Will replace dashes with underscores or underscores with dashes for the keywords in a map
  ;; Ignores String values in a map (both keys and values)
  ([from to] (replace-char-gen from to #{}))
  ([from to exceptions]
   #(if (keyword? %) (replace-char % from to exceptions) %)))

(defn dash->underscore [json-doc]
  (walk/postwalk (replace-char-gen \- \_) json-doc))

(defn underscore->dash [json-doc]
  (walk/postwalk (replace-char-gen \_ \- underscore->dash-exceptions) json-doc))

(defn write-json [data]
  (-> data
      dash->underscore
      json/generate-string))

(defn read-json [data]
  (-> data
      underscore->dash
      (json/parse-string true)))

(defn read-edn [v]
  (if (string? v) (edn/read-string {:readers *data-readers*} v) v))

(defn write-edn
  [v]
  (pr-str v))
