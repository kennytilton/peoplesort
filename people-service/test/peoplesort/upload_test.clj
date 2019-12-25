(ns peoplesort.upload-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [peoplesort.handler :refer [app]]
            [peoplesort.base :refer :all]
            [peoplesort.http :refer :all]
            [peoplesort.upload :as up]
            [peoplesort.test-utility :as util]))

(deftest happy-path-store-and-retrieve
  (testing "simple add and retrieve new record, then add more, then reset"
    (let [response (util/rqpost "/records/reset")]
      (is (= (:status response) Response-OK))
      (is (= (response-body->map response)
            {:new-count 0})))
    (let [response (util/postperson "Smith | Bob | male | green | 2011-08-23")]
      (is (= (:status response) Response-OK))
      (is (= (response-body->map response)
            {:new-count 1})))
    (let [response (util/rqget "/records/name")]
      (is (= (:status response) Response-OK))
      (is (= (response-body->map response)
            [{:LastName "Smith", :FirstName "Bob", :Gender "male",
              :FavoriteColor "green",
              :DateOfBirth "08/23/2011"}])))
    (let [response (util/postperson "BeebleBrox Zaphod male gold 2098-1-19")]
      (is (= (:status response) Response-OK))
      (is (= (response-body->map response)
            {:new-count 2})))
    (let [response (util/postpersons
                     "Lama, Dalai, male, saffron, 1935-7-6"
                     "Turner | Tina | female | saphireBlue | 1939-11-26")]
      (is (= (:status response) Response-OK))
      (is (= (response-body->map response)
            {:new-count 4})))

    ;; OK, one non-happy test...
    (let [response (util/postpersons
                     "Turner # Tina | female | saphireBlue | 1939-11-26")]
      (is (= (:status response) Unprocessable-Entity)))

    (let [response (util/rqget "/records/count")]
      (is (= (:status response) Response-OK))
      (is (= (response-body->map response)
            {:count 4})))

    (let [response (util/rqget "/records/name")]
      (is (= (:status response) Response-OK))
      (is (= (map :LastName (response-body->map response))
            ["BeebleBrox" "Lama" "Smith" "Turner"])))

    (let [response (util/rqpost "/records/reset")]
      (is (= (:status response) Response-OK))
      (is (= (response-body->map response)
            {:new-count 0})))))
