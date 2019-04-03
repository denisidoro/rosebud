(ns quark.beta.math.core)

(defn log
  [base x]
  (/ (Math/log x)
     (Math/log base)))

