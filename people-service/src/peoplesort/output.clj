(ns peoplesort.output
  (:require [clojure.data.json :as json]
            [peoplesort.persistence :as ps]
            [peoplesort.http :as http :refer :all]
            [peoplesort.sorting :refer :all]
            [peoplesort.base :refer [person-properties person-property]]))

(defn external-format
  "Return a record still as a map but with internal values
   such as dates converted back to external format."
  [record]
  (into {}
    (map (fn [spec]
           [(:name spec)
            ((or (:formatter spec) identity) (get record (:name spec)))])
      person-properties)))

(defn people-count [req]
  (with-exception-trap
    (respond-ok {:count (ps/record-count)})))

(defn compare-property
  ([prop-name] (compare-property prop-name :asc))
  ([prop-name sort-order]
   (fn [a b]
     ;; todo lose next two airbags
     ;; these slow things down and make the utility less flexible, but
     ;; during dev one commonly gets the key name wrong if only by typo
     (assert (contains? a prop-name))
     (assert (contains? b prop-name))
     (let [av (prop-name a)
           bv (prop-name b)]
       ;; implement asc/dsc option by flipping operands for dsc
       (let [[a b] (case sort-order
                     :asc [av bv]
                     :dsc [bv av]
                     (throw (Exception. (str "Invalid order: " sort-order))))
             comparator (or ((comp :comparator prop-name) person-property)
                          compare)]
         (comparator a b))))))

(defn order-by [rows & order-specs]
  (apply nested-sort rows
    (map #(apply compare-property %) order-specs)))

(defn stored-persons [& order-specs]
  (with-exception-trap
    (respond-ok
      (map external-format
        (apply order-by (ps/contents) order-specs)))))

(defn stored-persons-ordered-by
  "A generic endpoint allowing arbitrary and nested ordering
  to be specified in the request. Goal is to avoid forever hard-coding
  endpoints for combinations of properties and orderings."
  [req]
  (with-exception-trap
    (apply stored-persons
      (let [{:keys [sortkeys]} (:params req)]
        (mapv #(mapv keyword %)
          (json/read-str sortkeys))))))

(defn stored-persons-by-birthdate [req]
  (stored-persons [:DateOfBirth :asc]))

(defn stored-persons-by-name [req]
  (stored-persons [:LastName :asc] [:FirstName :asc]))

(defn stored-persons-by-gender [req]
  (stored-persons [:Gender :asc]))