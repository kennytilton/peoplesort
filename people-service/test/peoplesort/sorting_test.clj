(ns peoplesort.sorting-test
  (:require
    [clojure.test :refer :all]
    [peoplesort.sorting :refer :all]
    [peoplesort.properties :refer :all]
    [peoplesort.http :refer :all]
    [peoplesort.test-utility :as util]
    [clojure.data.json :as json]
    [clj-time.core :as tm]))

(deftest basic-nested-sort
  (testing "numeric sorts"
    (is (= (nested-sort [13 -3 0 5 2 -7 -8 6 -6]
             (compare-unary even?)
             (compare-binary >))
          [6 2 0 -6 -8 13 5 -3 -7]))
    (is (= (nested-sort [13 -3 0 5 2 -7 -8 6 -6]
             (compare-unary even?)
             (compare-unary neg?))
          [-8 -6 0 2 6 -3 -7 13 5]))))

(defn initialize-to-standard-crew
  "Clear datastore then load with fixed test data"
  []
  (util/rqreset!)
  (doseq [person ["Smith | Bob | male | green | 2011-08-23"
                  "BeebleBrox Zaphodra female gold 2098-1-19"
                  ;; n.b. next two dates will sort in wrong order as strings
                  "Lama, Dalai, male, saffron, 1939-9-6"
                  "Turner | Tina | female | saphireBlue | 1939-11-26"
                  "Turner Bachman male various 1973-06-30"
                  "Turner | Ted | male | gray | 1938-11-19"]]
    (let [response (util/postperson person)]
      (is (= (:status response) Response-OK)))))

(deftest built-in-gender-sort
  (testing "Ascending gender endpoint"
    (initialize-to-standard-crew)
    (let [response (util/rqget "/records/gender")]
      (is (= (:status response) Response-OK))
      (is (= (map :Gender (response-body->map response))
        ["female" "female" "male" "male" "male" "male"])))))

(deftest built-in-birthdate-sort
  (testing "Ascending birthdate endpoint"
    (initialize-to-standard-crew)
    (let [response (util/rqget "/records/birthdate")]
      (is (= (:status response) Response-OK))
      (is (= (map (juxt :LastName :FirstName) (response-body->map response))
            [["Turner" "Ted"]
             ["Lama" "Dalai"]
             ["Turner" "Tina"]
             ["Turner" "Bachman"]
             ["Smith" "Bob"]
             ["BeebleBrox" "Zaphodra"]])))))

(deftest nested-but-hardcoded-sort
  (testing "Nested sort, last name then first"
    (initialize-to-standard-crew)
    (let [response (util/rqget "/records/name")]
      (is (= (:status response) Response-OK))
      (is (= (map (juxt :LastName :FirstName) (response-body->map response))
            [["BeebleBrox" "Zaphodra"]
             ["Lama" "Dalai"]
             ["Smith" "Bob"]
             ["Turner" "Bachman"]
             ["Turner" "Ted"]
             ["Turner" "Tina"]])))))

(deftest nested-custom-sort
  (testing "Nested programmed sort: Gender asc, last name dsc, then first asc"
    (initialize-to-standard-crew)
    (let [response (util/rqget "/records/orderedby"
                     {:sortkeys (json/write-str
                                  [[:Gender :asc] [:LastName :dsc]
                                   [:FirstName :asc]])})]
      (is (= (:status response) Response-OK))
      (is (= (map (juxt :LastName :FirstName) (response-body->map response))
            [["Turner" "Tina"]
             ["BeebleBrox" "Zaphodra"]
             ["Turner" "Bachman"]
             ["Turner" "Ted"]
             ["Smith" "Bob"]
             ["Lama" "Dalai"]])))))