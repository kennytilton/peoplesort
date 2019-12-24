(ns peoplesort.test-utility
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
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

(defn postperson [raw]
  (rqpost "/records" {:raw raw}))

(defn postpersons [& persons]
  (rqpost "/records/bulk" {:persons persons}))


