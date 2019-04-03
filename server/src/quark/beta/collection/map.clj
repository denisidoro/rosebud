(ns quark.beta.collection.map
  (:require [clojure.string :as str]
            [quark.collection.map :as map]))

(defn get-by-substr
  [body substr]
  (some->> body
           (map/filter-keys #(str/includes? % substr))
           vals
           first))

(defn get-in-by-substr
  [body path]
  (reduce (fn [b p] (get-by-substr b p)) body path))
