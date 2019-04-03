(ns rosebud.service-test
  (:require [clojure.test :as t]
            [matcher-combinators.midje :refer [match]]
            [midje.sweet :refer :all]
            [rosebud.components.system :as components.system]
            [rosebud.http-helpers :refer [GET POST]]))

(components.system/ensure-system-up! :test)

;; TODO: fix this
(def body
  {:targets [{:target (str {:resolver :bucket/balance})
              :refId  :A
              :type   :timeserie}]})

;; TODO: fix this
(defn some-pos-data
  [body]
  (-> body first :datapoints first second pos?))

;; TODO: fix this
(t/deftest service-starts
  (fact "hitting home page endpoint"
    (GET "/" body 200) => (match {:body some?})))

