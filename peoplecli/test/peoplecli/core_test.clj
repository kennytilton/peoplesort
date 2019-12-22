(ns peoplecli.core-test
  (:require [clojure.test :refer :all]
            [peoplecli.core :refer :all]))

(deftest header-parsing
  ;; note that the parse is actually applicable to any row, but only
  ;; for the header do we tolerate multiple possible delmiters.

  (testing "valid headers"
    ;(prn :bam (header-parse "|," 5 "Smith, Bob, male, green, 05/03/2001"))
    (let [[delim elts] (header-parse "|, " 5 "Smith, Bob, male, green, 05/03/2001")]
      (is (= (str delim) (str (re-pattern "\\,"))))
      (is (= elts ["Smith", "Bob", "male", "green", "05/03/2001"])))

    (let [[delim elts] (header-parse "|, " 5 "Smith | Bob| male|green|05/03/2001")]
      (is (= (str delim) (str (re-pattern "\\|"))))
      (is (= elts ["Smith", "Bob", "male", "green", "05/03/2001"])))

    (let [[delim elts] (header-parse "|, " 5 "Smith Bob male green 05/03/2001")]
      (is (= (str delim) (str (re-pattern "\\ "))))
      (is (= elts ["Smith", "Bob", "male", "green", "05/03/2001"]))))

  (testing "invalid delim"
    ;(prn :bam (header-parse "|," 5 "Smith, Bob, male, green, 05/03/2001"))
    (let [parse (header-parse "|# " 5 "Smith,Bob,male,green,05/03/2001")]
      (is (nil? parse))))

  (testing "invalid count"
    ;(prn :bam (header-parse "|," 5 "Smith, Bob, male, green, 05/03/2001"))
    (let [parse (header-parse "|, " 4 "Smith,Bob,male,green,05/03/2001")]
      (is (nil? parse)))))
