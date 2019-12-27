(ns peoplesort.upload
  (:require [clojure.string :as str]
            [peoplesort.properties :refer :all]
            [peoplesort.http :as http]
            [peoplesort.persistence :as ps]))

(def SUPPORTED-DELIMS [#"\|" #"," #" "])

(defn person-csv-parse
  "Try each supported delimiter looking for one that splits the input row
  into the right number of properties which, after trimming, pass
  any parsing function specified for said property."
  [input-row]
  (letfn [(try-delim [col-delim]
            (try
              (let [col-values (mapv str/trim
                                 (str/split input-row col-delim))]
                (when (= (count col-values) (count person-properties))
                  {:success true
                   :record  (into {}
                              ;; optimistically parse each value. Parsers
                              ;; throw exceptions when unhappy.
                              (map (fn [val spec]
                                     [(:name spec)
                                      ((or (:parser spec) identity) val)])
                                col-values person-properties))}))
              (catch Exception e nil)))]
    (or (some try-delim SUPPORTED-DELIMS)
      {:success false
       :reason  (str "Error parsing: " input-row)})))

;;; --- the endpoint implementations --------------------------

(defn people-reset!
  [req]
  (http/with-exception-trap
    (ps/store-reset!)
    ;; return honest count instead of assuming it is zero
    (http/respond-ok {:new-count (ps/record-count)})))

(defn person-add-one [req]
  (http/with-exception-trap
    (let [{:keys [person]} (:params req)]
      (cond
        (str/blank? person)
        (http/respond-data-error "Person data blank.")

        :default
        (let [parse (person-csv-parse person)]
          (if (:success parse)
            (do
              (ps/write! (:record parse))
              (http/respond-ok {:new-count (ps/record-count)}))
            (http/respond-data-error (:reason parse))))))))

(defn persons-add-bulk [req]
  (http/with-exception-trap
    (let [{:keys [persons]} (:params req)
          parses (map person-csv-parse persons)]
      (cond
        (every? :success parses)
        (do (ps/write-bulk! (map :record parses))
            (http/respond-ok {:new-count (ps/record-count)}))
        :default
        (http/respond-data-error (str/join "\n"
                                   (map :reason (remove :success parses))))))))