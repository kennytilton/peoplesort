(ns peoplesort.sorting-test
  (:require
    [clojure.test :refer :all]
    [peoplesort.sorting :refer :all]
    [peoplesort.http :refer :all]
    [peoplesort.test-utility :as util]))

(deftest basic-sort
  (testing "numeric sorts"
    (is (= (nested-sort [13 -3 0 5 2 -7 -8 6 -6]
             (compare-unary even?)
             (compare-binary >))
          [6 2 0 -6 -8 13 5 -3 -7]))
    (is (= (nested-sort [13 -3 0 5 2 -7 -8 6 -6]
             (compare-unary even?)
             (compare-unary neg?))
          [-8 -6 0 2 6 -3 -7 13 5]))))

(deftest nested-but-hardcoded-sort
  (testing "Nested sort, last name then first"
    (let [response (util/rqpost "/records/reset")]
      (is (= (:status response) Response-OK)))

    (doseq [person ["Smith | Bob | male | green | 2011-08-23"
                    "BeebleBrox Zaphod male gold 2098-1-19"
                    "Lama, Dalai, male, saffron, 1935-7-6"
                    "Turner | Tina | female | saphireBlue | 1939-11-26"
                    "Turner Bachman male various 1973-06-30"
                    "Turner | Ted | male | gray | 1938-11-19"]]
      (let [response (util/postperson person)]
        (is (= (:status response) Response-OK))))

    (let [response (util/rqget "/records/name")]
      (is (= (:status response) Response-OK))
      (prn :names (response-body->map response))

      (is (= (map (juxt :LastName :FirstName) (response-body->map response))
            [["BeebleBrox" "Zaphod"]
             ["Lama" "Dalai"]
             ["Smith" "Bob"]
             ["Turner" "Bachman"]
             ["Turner" "Ted"]
             ["Turner" "Tina"]])))))