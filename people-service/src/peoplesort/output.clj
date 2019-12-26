(ns peoplesort.output
  (:require [clojure.data.json :as json]
            [peoplesort.persistence :as ps]
            [peoplesort.http :as http :refer :all]
            [peoplesort.sorting :refer :all]
            [peoplesort.properties :as props]))

(defn external-format
  "Return a record still as a map but with internal values such as dates
   converted back to external format."
  [record]
  (into {}
    (map (fn [spec]
           [(:name spec)
            (props/hashes-iff-error
              (get record (:name spec))
              (:formatter spec))])
      props/person-properties)))

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
     (let [[a b] (map prop-name [a b])]
       ;; implement asc/dsc option by flipping operands for dsc
       (let [[a b] (case sort-order
                     :asc [a b]
                     :dsc [b a]
                     (throw (Exception. (str "compare-property> Invalid sort order: " sort-order))))
             comparator (or ((comp :comparator prop-name) props/person-property)
                          compare)]
         (comparator a b))))))

(defn order-by
  [rows & order-specs]
  "Simulates SQL 'order by', allowing any number of properties/directions"
  (apply nested-sort rows
    (map #(apply compare-property %) order-specs)))


(defn stored-persons
  [& order-specs]
  "Workhorse function that produces this service's output, sorted and
  formatted as required by property specs and requested sort(s)."
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
  ;; The Step 1 exercise sorts female before male, as does this
  ;; exercise's comparator. This exercise did not specify ordering, so
  ;; we continue with female first, which in SQL-ese makes this
  ;; an ascending sort, which does not make sense for sort by gender.
  ;; Todo refactor so sort direction is not just asc or dsc, perhaps
  ;; by having a neutral option that implements as "whatever the comparator
  ;; says, treated as :asc".
  (stored-persons [:Gender :asc]))