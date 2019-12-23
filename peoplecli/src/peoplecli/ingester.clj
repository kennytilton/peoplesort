(ns peoplecli.ingester
  (:require [clojure.pprint :as pp]
            [clojure.string :as str]
            [clj-time.format :as tfm]
            [clojure.java.io :as io]
            [clojure.set :as set]
            [peoplecli.reporter :as rpt]))

(declare people-props-reqd)

(defn file-found? [path]
  (or (.exists (io/as-file path))
    (do (println (format "\nSuggested file <%s> not found.\n" path))
        false)))

(defn dob-parse [dob-in]
  "Convert from YYYY-mm-dd to Date object"
  (tfm/parse (tfm/formatter "yyyy-MM-dd") dob-in))

(defn dob-display [dob]
  "Convert Date object to mm/dd/YYYY"
  (tfm/unparse (tfm/formatter "MM/dd/yyyy") dob))

(defn header-parse
  [allowed-delims col-specs header-string]
  (some #(let [col-delim (re-pattern (str "\\" %))
               col-headers (mapv (comp keyword str/trim)
                             (str/split header-string col-delim))
               headers-missing (set/difference
                                 (set (keys col-specs))
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

#_(people-input-analyze people-props-reqd "resources/spaces.csv")

(defn people-input-analyze [col-specs filepath]
  (with-open [rdr (io/reader filepath)]
    (when-let [header-def (header-parse "|, " col-specs (first (line-seq rdr)))]
      (prn :hdef header-def)
      (merge header-def {:filepath   filepath
                         :col-specs col-specs}))))

(def people-col-order [:last :first :dob :gender :color])

(defn people-file-ingest [{:keys [filepath col-delim col-specs col-headers]
                           :as   input-spec}]
  (with-open [rdr (io/reader filepath)]
    (into []                                                ;; trick to realize all before closinr reader
      (map-indexed (fn [row-no row]
                     (let [col-values (map str/trim (str/split row col-delim))]
                       (when (< (count col-values) (count col-headers))
                         (throw (Exception. (str "Insufficient column count " (count col-values)
                                              " at row " (inc row-no)
                                              " in file " filepath))))
                       (let [col-values (zipmap col-headers col-values)]
                         ;; parse each value while also standardizing column order
                         ;; which we now allow to vary in input files
                         (map (fn [col-header]
                                (let [col-spec (col-header col-specs)]
                                  (prn :hdr  col-header col-spec (col-header col-values))
                                  (try
                                    ((or (:parser col-spec) identity)
                                     (col-header col-values))
                                    (catch Exception e
                                      "#####"))))
                           people-col-order))))
        (rest (line-seq rdr))))))

#_(people-file-ingest
    (people-input-analyze
      people-props-reqd
      "resources/spaces.csv"))

(def people-props-reqd
  {:first  {:label "Given name"
            :format-field "~20a"}
   :last   {:label "Surname":format-field "~20a"}
   :gender {:label "Gender"
            :format-field "~10a"
            :parser       (fn [g]
                            (or (some #{g} ["male" "female"])
                              (throw (Exception. (str "Invalid gender: " g)))))}
   :color  {:label "Favorite color"
            :format-field "~15a"}
   :dob    {:label "Born"
            :format-field "~16a"
            :parser       dob-parse
            :formatter    dob-display}})

#_ (process-inputs ["resources/spaces.csv"] #(pp/pprint %2))
#_ (process-inputs ["resources/spaces.csv"] rpt/people-report)

(defn process-inputs
  ([input-files]
   (process-inputs input-files nil))

  ([input-files reporter]
   (let [input-specs (map #(people-input-analyze people-props-reqd %) input-files)]
     (when-not (some nil? input-specs)
       (let [parsed-rows (distinct
                           ;; ^^^ seems right behavior to de-dupe
                           (mapcat people-file-ingest input-specs))]
         (prn :parsed parsed-rows)
         (when reporter
           ;; parsed rows have values ordered as required, now
           ;; pull each col spec into a vector in the same order
           ;; and feed reporter data in efficient form
           (reporter
             (map #(% people-props-reqd) people-col-order)
             parsed-rows)))))))
