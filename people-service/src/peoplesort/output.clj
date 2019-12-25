(ns peoplesort.output
  (:require [peoplesort.persistence :as ps]
            [peoplesort.http :as http :refer :all]
            [peoplesort.base :refer [people-props-reqd]]))

(defn external-format
  "Return a record still as a map but with internal values
   such as dates converted back to external format."
  [record]
  (into {}
    (map (fn [spec]
           [(:name spec)
            ((or (:formatter spec) identity) (get record (:name spec)))])
      people-props-reqd)))

(defn people-count [req]
  (without-exception
    (respond-ok {:count (ps/record-count)})))

;; todo DRY these
(defn stored-persons [& order-specs]
  (without-exception
    (respond-ok
      (map external-format
        (apply ps/order-by (ps/contents)order-specs)))))

(defn stored-persons-ordered-by [req]
  (without-exception
    (let [{:keys [orderby]} (:params req)]
      (prn :order-by! orderby)
      (respond-ok
        (ps/order-by (ps/contents) orderby)))))

(defn stored-persons-by-dob [req]
  (stored-persons [:DateOfBirth :asc]))

(defn stored-persons-by-name [req]
  (stored-persons [:LastName :asc] [:FirstName :asc]))

(defn stored-persons-by-gender [req]
  (stored-persons [:Gender :asc]))