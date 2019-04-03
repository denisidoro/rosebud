(ns fundo.logic.core
  (:require [clj-time.format :as time.format]
            [quark.beta.math.point :as point]
            [quark.beta.time :as time]))

(def ^:private formatter (time.format/formatter "yyyyMMdd"))

(defn ^:private fundo-entry->point
  [{:keys [c d p q]}]
  (point/new
   (time/date-str->millis (str d) formatter)
   c))

(defn as-bucket
  [cnpj body]
  {:bucket/id (keyword "fundo" (str cnpj))
   :bucket/path [:fundo cnpj]
   :history/gross (map fundo-entry->point body)})
