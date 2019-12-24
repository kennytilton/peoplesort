(ns peoplesort.test-utility
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [peoplesort.handler :refer [app]]))

(defn appget
  ([ept]
   (appget ept nil))
  ([ept query]
   (app
     (mock/request :get
       ept query))))

(defn apppost
  ([ept]
   (appget ept nil))
  ([ept body]
   (apppost
     (mock/request :post
       ept body))))

(defn postperson [raw]
  (apppost "/records" {:raw raw}))


