(ns peoplesort.test-utility
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [peoplesort.http :refer :all]
            [peoplesort.handler :refer [app]]))

(defn rqget
  ([ept]
   (rqget ept nil))
  ([ept query]
   (app
     (mock/request :get
       ept query))))

(defn rqpost
  ([ept]
   (rqpost ept nil))
  ([ept body]
   (app
     (mock/request :post
       ept body))))

(defn rqreset! []
  (let [response (rqpost "/records/reset")]
    (assert (= (:status response) Response-OK))))

(defn postperson [person]
  (rqpost "/records" {:person person}))

(defn postpersons [& persons]
  (rqpost "/records/bulk" {:persons persons}))

(defmacro is-response-ok [response-form]
  `(is (= Response-OK (:status ~response-form))))

(defmacro is-unprocessable [response-form]
  `(is (= Unprocessable-Entity (:status ~response-form))))

(defmacro is-body-count [ct response-form]
  `(let [response# ~response-form]
     (is-response-ok response#)
     (is (= (response-body->map response#)
           {:new-count ~ct}))))
