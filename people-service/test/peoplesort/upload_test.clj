(ns peoplesort.upload-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [peoplesort.handler :refer [app]]
            [peoplesort.utility :refer :all]))

(deftest upload
  (testing "simple new record"
    (let [response (postperson "Smith | Bob | male | green | 2011-08-23")]
      (is (= (:status response) Response-OK))
      (is (= (body->map response)
            {:new-count 1})))
    (let [response (postperson "BeebleBrox | Zaphod | male | gold | 2098-1-19")]
      (is (= (:status response) Response-OK))
      (is (= (body->map response)
            {:new-count 2})))
    (let [response (appget "/records/count")]
      (is (= (:status response) Response-OK))
      (is (= (body->map response)
            {:count 2})))
    (let [response (apppost "/records/reset")]
      (pprt :respo response)
      (is (= (:status response) Response-OK))
      (is (= (body->map response)
            {:new-count 0})))))
