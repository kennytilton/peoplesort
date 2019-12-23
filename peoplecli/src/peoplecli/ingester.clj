(ns peoplecli.ingester
  (:require [clojure.pprint :as pp]
            [clojure.string :as str]
            [clj-time.format :as tfm]
            [clojure.java.io :as io]
            [clojure.set :as set]))

(defn file-found? [path]
  (or (.exists (io/as-file path))
    (do (println (format "\nSuggested file <%s> not found.\n" path))
        false)))

(defn dob-parse [dob-in]
  "Convert from YYYY-mm-dd to Date object"
  (tfm/parse (tfm/formatter "yyyy-MM-dd") dob-in))

(defn header-parse
  [allowed-delims prop-specs header-string]
  (some #(let [col-delim (re-pattern (str "\\" %))
               col-headers (mapv (comp keyword str/trim)
                             (str/split header-string col-delim))
               headers-missing (set/difference
                                 (set (map :name prop-specs))
                                 (set col-headers))]
           (when (and (empty? headers-missing)
                   ;; now that we support excess columns, testing the space
                   ;; as a delimiter can be a mess if the header contains
                   ;; multiple spaces or an unsupported delimiter such as #.
                   (every? (fn [hdr] (re-matches #"[a-zA-Z0-9-_\.]+"
                                       (name hdr)))
                     col-headers))
             {:col-delim   col-delim
              :col-headers col-headers}))
    allowed-delims))

(defn people-file-validate [prop-specs filepath]
  (with-open [rdr (io/reader filepath)]
    (when-let [header-def (header-parse "|, " prop-specs (first (line-seq rdr)))]
      (prn :hdef header-def)
      (merge header-def {:filepath   filepath
                         :prop-specs prop-specs}))))

#_(people-file-validate {:name :first}
                         {:name :last}
                         {:name :gender}
                         {:name :color}
                         {:name   :dob
                          :parser dob-parse}]
    "resources/spaces.csv")

#_ (people-file-ingest
     (people-file-validate [{:name :first}
                            {:name :last}
                            {:name :gender}
                            {:name :color}
                            {:name   :dob
                             :parser dob-parse}]
       "resources/pipes.csv"))
(defn people-file-ingest [{:keys [filepath col-delim prop-specs col-headers]
                           :as   input-spec}]
  (prn :pfi-specs prop-specs)
  (with-open [rdr (io/reader filepath)]
    (into []                                                ;; trick to realize all before closinr reader
      (map-indexed (fn [row-no row]
                     ;(prn :mapi row-no row)
                     (let [col-values (map str/trim (str/split row col-delim))]
                       ;(prn :rpa row-no cols)
                       (when (< (count col-values) (count col-headers))
                         (throw (Exception. (str "Insufficient column count " (count col-values)
                                              " at row " (inc row-no)
                                              " in file " filepath))))
                       (into []
                         (map (fn [col-value col-header]
                                (let [col-spec (col-header prop-specs)]
                                  ;(prn :hdr col-value col-header col-spec)
                                  (when-let [p (:parser col-spec)]
                                    (prn :p p))
                                  (try
                                    (or (:parser col-spec) col-value)
                                    (catch Exception e
                                      "#####"))))
                           col-values col-headers))))
        (line-seq rdr)))))

(defn process-inputs
  ([input-files props-reqd]
   (process-inputs input-files props-reqd nil))

  ([input-files props-reqd reporter]
   (let [input-specs (map #(people-file-validate props-reqd %) input-files)]
     (when-not (some nil? input-specs)
       (let [parsed-rows (distinct
                           ;; ^^^ seems right
                           (mapcat people-file-ingest input-specs))]
         (when reporter
           (reporter parsed-rows)))))))

