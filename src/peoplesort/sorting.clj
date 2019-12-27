(ns peoplesort.sorting)

(defn nested-sort-comparator
  "Generates a nested sort comparator from multiple sorts
  by trying the first test and continuing to the next
  iff the test is a tie, returning 0 (tied) if no test
  finds the two operands different."
  [& sorts]
  (fn [a b]
    (loop [[s & more-sorts] sorts]
      (cond
        (nil? s) 0
        :default
        (let [comparison (s a b)]
          (if (zero? comparison)
            (recur more-sorts)
            comparison))))))

(defn compare-unary
  "Generates a comparator that orders first a parameter
   satisfying the unary test, iff the other does not.
   Otherwise declares a tie. Sort of like an XOR."
  [unary-boolean]
  (fn [a b]
    (if (unary-boolean a)
      (if (unary-boolean b)
        0 -1)
      (if (unary-boolean b)
        1 0))))

(defn compare-binary
  [binary-boolean]
  "Generates a comparator that orders one parameter p' before
  a second parameter p'' only where (test p' p'') is true
  and (test p'' p') is not."
  (fn [a b]
    (if (binary-boolean a b)
      (if (binary-boolean b a)
        0 -1)
      (if (binary-boolean b a)
        1 0))))

(defn nested-sort [coll & sorts]
  (sort (apply nested-sort-comparator sorts) coll))