(ns peoplesort.sorting)

(defn nested-sort-comparator
  "Generates a nested sort comparator from multiple sorts"
  [& sorts]
  (fn [a b]
    ;;(prn :ab a b)
    (loop [[s & more-sorts] sorts]
      (cond
        (nil? s) 0
        :default
        (let [comparison (s a b)]
          (if (zero? comparison)
            (recur more-sorts)
            comparison))))))

(defn compare-unary
  "Generates comparator that orders first an only parameter
   that satisfies the boolean test."
  [unary-boolean]
  (fn [a b]
    (if (unary-boolean a)
      (if (unary-boolean b)
        0 -1)
      (if (unary-boolean b)
        1 0))))

(defn compare-binary
  [binary-boolean]
  "Generates comparator that orders one parameter p' before p''
  only where (test p' p'') is true and (test p'' p') is not."
  (fn [a b]
    (if (binary-boolean a b)
      (if (binary-boolean b a)
        0 -1)
      (if (binary-boolean b a)
        1 0))))

(defn compare-property
  ([prop-name] (compare-property prop-name :asc compare))
  ([prop-name sort-order] (compare-property prop-name sort-order compare))
  ([prop-name sort-order comparator]
   (fn [a b]
     ;; todo lose next two airbags
     (assert (contains? a prop-name))
     (assert (contains? b prop-name))
     (let [av (prop-name a)
           bv (prop-name b)]
       (let [[a b] (case sort-order
                     :asc [av bv]
                     :dsc [bv av]
                     (throw (Exception. (str "Invalid order: " sort-order))))]
         (comparator a b))))))

#_(nested-sort [{:x 1 :y 3} {:x 1 :y 2} {:x 42 :y 17}]
    (compare-property :x :dsc) (compare-property :y))

(defn nested-sort [coll & sorts]
  (let [c (apply nested-sort-comparator sorts)]
    (prn :c c)
    (sort c coll)))

#_ (nested-sort
     [{:LastName "Turner", :FirstName "Ted", :Gender "male", :FavoriteColor "gray", :DateOfBirth "11/19/1938"}
      {:LastName "Turner", :FirstName "Bachman", :Gender "male", :FavoriteColor "various", :DateOfBirth "06/30/1973"}
      {:LastName "Turner", :FirstName "Tina", :Gender "female", :FavoriteColor "saphireBlue", :DateOfBirth "11/26/1939"}
      {:LastName "Lama", :FirstName "Dalai", :Gender "male", :FavoriteColor "saffron", :DateOfBirth "07/06/1935"}
      {:LastName "BeebleBrox", :FirstName "Zaphod", :Gender "male", :FavoriteColor "gold", :DateOfBirth "01/19/2098"}
      {:LastName "Smith", :FirstName "Bob", :Gender "male", :FavoriteColor "green", :DateOfBirth "08/23/2011"}]
     (compare-property :LastName)
     (compare-property :FirstName :dsc))