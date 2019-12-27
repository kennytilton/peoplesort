(ns peoplesort.handler-test
  (:require [clojure.test :refer :all]
            [clojure.pprint :as pp]
            [ring.mock.request :as mock]
            [peoplesort.core :refer :all]
            [peoplesort.http :refer :all]))

(deftest test-app
  (testing "bad route"
    (let [response (app (mock/request :get "/invalid"))]
      (is (= (:status response) Not-Found)))))



