(ns quark.beta.crypto)

(defn offset
  [off text]
  (->> text
       (map int)
       (map #(+ off %))
       (map char)
       (apply str)))
