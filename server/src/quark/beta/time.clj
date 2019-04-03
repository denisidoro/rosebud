(ns quark.beta.time
  (:refer-clojure :exclude [compare])
  (:require [clj-time.coerce :as time.coerce]
            [clj-time.core :as time]
            [clj-time.format :as time.format]
            [clj-time.periodic :as time.periodic]
            [clojure.string :as str])
  (:import (org.joda.time DateTime)))

(def business-days-in-a-year 252)
(def days-in-a-year 365)

(defn days-between-dates
  [date1 date2]
  (let [d1         (time.coerce/to-date-time date1)
        d2         (time.coerce/to-date-time date2)
        ascending? (time/before? d1 d2)
        newest     (if ascending? d2 d1)
        oldest     (if ascending? d1 d2)
        difference (time/in-days (time/interval oldest newest))]
    (if ascending?
      difference
      (- difference))))

(def approximate-business-days-between-dates
  (comp (partial * (/ business-days-in-a-year days-in-a-year))
        days-between-dates))

(defn compare
  [t1 t2]
  (cond (time/before? t1 t2) -1
        (= t1 t2) 0
        :else 1))

(def default-unparse-formatter
  (time.format/formatter "dd MMM yyyy"))

(def default-parse-formatter
  (time.format/formatter "yyyy-MM-dd"))

(defn date-time->string
  ([as-of-time]
   (date-time->string as-of-time default-unparse-formatter))
  ([as-of-time formatter]
   (time.format/unparse formatter as-of-time)))

(def ^:private translation-map
  {"january"   "janeiro"
   "february"  "fevereiro"
   "march"     "março"
   "april"     "abril"
   "may"       "maio"
   "june"      "junho"
   "july"      "julho"
   "august"    "agosto"
   "september" "setembro"
   "october"   "outubro"
   "november"  "novembro"
   "december"  "dezembro"
   "sunday"    "domingo"
   "monday"    "segunda"
   "tuesday"   "terça"
   "wednesday" "quarta"
   "thursday"  "quinta"
   "friday"    "sexta"
   "saturday"  "sábado"})

(defn ^:private translate
  [text]
  (reduce (fn [t [k v]] (str/replace t k v)) (str/lower-case text) translation-map))

(defn months-between-ms
  [oldest newest]
  (int (/ (- newest oldest)
          (* 1000 60 60 24 30))))

(defn pretty-weekday
  [as-of]
  (-> "dd 'de' MMMM 'de' yyyy, EEEE"
      time.format/formatter
      (time.format/unparse as-of)
      translate))

(defn date-str->date-time
  ([as-of] (date-str->date-time as-of default-parse-formatter))
  ([as-of formatter]
   (time.format/parse formatter as-of)))

(defn date-str->millis
  ([as-of] (date-str->millis as-of default-parse-formatter))
  ([as-of formatter]
   (-> (date-str->date-time as-of formatter)
       time.coerce/to-long)))

(defn as-millis
  [x]
  (time.coerce/to-long x))

(defn from-millis
  [millis]
  (time.coerce/from-long millis))

(defn now-millis
  []
  (time.coerce/to-long (time/now)))

(defn millis->the-day-after
  [as-of-time]
  (-> as-of-time
      time.coerce/from-long
      (time/plus (time/days 1))
      time.coerce/to-long))

(defn millis->the-day-before
  [as-of-time]
  (-> as-of-time
      time.coerce/from-long
      (time/minus (time/days 1))
      time.coerce/to-long))

(defn end-of-month-seq
  [from]
  (->> (time.periodic/periodic-seq from (time/months 1))
       (map (fn [^DateTime time] (-> time
                                     (.withDayOfMonth 1)
                                     (.minusDays 1))))))
