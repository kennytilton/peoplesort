(ns peoplesort.upload
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [clojure.data.json :as json]
    ;;[ring.util.response :refer [response]]
            [peoplesort.base :refer :all]
            [peoplesort.http :as http]
            [peoplesort.persistence :as ps]))

(def SUPPORTED-DELIMS [#"\|" #"," #" "])

(defn person-csv-parse
  [input-row]
  (some (fn [col-delim]
          (try
            (let [col-values (mapv str/trim
                               (str/split input-row col-delim))]
              (when (= (count col-values)
                      (count people-props-reqd))
                (prn :count-ok! col-values)
                {:success true
                 ;; next we impose the required ordering of the columns; an
                 ;; alternative would be for the API to accept an initial
                 ;; row of colun labels, just as in a CSV
                 :record  (into {}
                            (map (fn [val spec]
                                   [(:name spec)
                                    ((or (:parser spec) identity) val)])
                              col-values people-props-reqd))}))
            (catch Exception e
              (prn :xxx e)
              nil)))
    SUPPORTED-DELIMS))

#_ (person-csv-parse "1 | 2 | male | 4 | 2019-12-24")

(defn people-reset!
  [req]
  (http/without-exception
    (do
      (ps/store-reset!)
      ;; return honest count...
      (http/respond-ok {:new-count (ps/record-count)}))))

(defn person-add-one [req]
  (prn :add-one req)
  (http/without-exception
    (let [{:keys [raw]} (:params req)]
      (prn :addraw raw)
      (cond
        (some str/blank? [raw])
        (http/respond-data-error "Person data blank.")

        :default
        (let [parse (person-csv-parse raw)]
          (pprt :parse parse)
          (if (:success parse)
            (do
              (ps/write! (:record parse))
              (http/respond-ok {:new-count (ps/record-count)}))
            (http/respond-data-error (:error parse))))))))

(defn person-add-bulk [req]
  (http/without-exception
    (let [{:keys [persons]} (:params req)
          parses (map person-csv-parse persons)]
      (cond
        (every? seq parses)
        (do (ps/write-bulk! (map :record parses))
            (http/respond-ok {:new-count (ps/record-count)}))

        :default
        (http/respond-data-error (str/join "\n"
                                   (map :reason (remove seq parses))))))))
