(ns peoplesort.handler-test
  (:require [clojure.test :refer :all]
            [clojure.pprint :as pp]
            [cheshire.core :refer :all]
            [ring.mock.request :as mock]
            [peoplesort.handler :refer :all :as psh]))

(deftest test-app
  (testing "bad route"
    (let [response (app (mock/request :get "/invalid"))]
      ;(prn :repo response)
      (is (= (:status response) Not-Found)))))



