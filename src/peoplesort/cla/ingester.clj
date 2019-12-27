;;
;; In CLA mode, processing filepaths supplied on CL
;;
(ns peoplesort.cla.ingester
  (:require
    [clojure.pprint :as pp]
    [clojure.string :as str]
    [clj-time.format :as tfm]
    [clojure.java.io :as io]
    [clojure.set :as set]
    [peoplesort.properties :as props]
    [peoplesort.cla.reporter :as rpt]))

;;; ---- utilities --------------------------------------------------

(defn file-found? [path]
  (or (.exists (io/as-file path))
    (do (println (format "\nSuggested file <%s> not found.\n" path))
        false)))

;; --- input file header parsing -------------------------------------------

(defn header-parse
  "Parse a standard CSV header where first row names the
  attribute represented by a column, allowing extra colummns
  while requiring a minimum set. Also allow columns to
  appear in any order.

  Returns a map describing which delimiter succeeded and
  the column information."
  [allowed-delims col-specs header-string]

  (some #(let [col-delim %
               col-headers (mapv (comp keyword str/trim)
                             (str/split header-string col-delim))
               headers-missing (set/difference
                                 (set (map :name col-specs))
                                 (set col-headers))]
           (when (and (empty? headers-missing)
                   ;; now that we support excess columns, testing the space
                   ;; as a delimiter can be a mess if the header contains
                   ;; multiple spaces or an unsupported delimiter such as #, so
                   ;; here we guard against nonsense headers that would arise.
                   (every? (fn [hdr] (re-matches #"[a-zA-Z0-9-_\.]+"
                                       (name hdr)))
                     col-headers))
             {:col-delim   col-delim
              :col-headers col-headers}))
    allowed-delims))

(defn people-input-analyze
  "Parse the header of an input file and, if successful, add
  filepath and column specs applied to make a full file descriptor
  for downstream processing."
  [col-specs filepath]
  (with-open [rdr (io/reader filepath)]
    (when-let [header-def (header-parse props/SUPPORTED-DELIMS col-specs (first (line-seq rdr)))]
      (merge header-def {:filepath   filepath
                         :col-specs col-specs}))))

#_
    (people-file-ingest
      (people-input-analyze props/person-properties "resources/pipes.csv"))

(def PEOPLE-COL-ORDER
  "Input files can vary column order, but we want the ingested
  data normalized to a particular order."
  [:LastName :FirstName :DateOfBirth :Gender :FavoriteColor])

(defn people-file-ingest [{:keys [filepath col-delim col-headers]
                           :as   input-file-analysis}]
  (with-open [rdr (io/reader filepath)]
    (into []
      ;; ^^^ trick to realize all before closing reader
      (map-indexed (fn [row-no row]
                     (let [col-values (map str/trim (str/split row col-delim))]
                       (when (< (count col-values) (count col-headers))
                         (throw (Exception. (str "Insufficient column count " (count col-values)
                                              " at row " (inc row-no)
                                              " in file " filepath))))
                       (let [col-values (zipmap col-headers col-values)]
                         ;; col-values is now a dictionary of input column
                         ;; names and values, which is read by column in the
                         ;; standard order for output. Record invalid
                         ;; values as :error for later rendering as "#####".
                         (map (fn [col-header]
                                (let [col-spec (col-header props/person-property-dictionary)]
                                  (try
                                    ((or (:parser col-spec) identity)
                                     (col-header col-values))
                                    (catch Exception e
                                      :error))))
                           PEOPLE-COL-ORDER))))
        ;; rest vvv discards header...
        (rest (line-seq rdr))))))

;;; --- the app, basically ------------------------------------

(defn ingest-files-and-report
  ([input-files]
   (ingest-files-and-report input-files nil))

  ([input-files reporter]
   (let [input-specs (map #(people-input-analyze props/person-properties %) input-files)]
     (when-not (some nil? input-specs)
       (let [parsed-rows (distinct
                           ;; ^^^ seems right behavior to de-dupe
                           (mapcat people-file-ingest input-specs))]
         (when reporter
           ;; parsed rows have values ordered as required, now
           ;; pull each col spec into a vector in the same order
           ;; and feed reporter data in efficient form
           (reporter
             (map #(% props/person-property-dictionary) PEOPLE-COL-ORDER)
             parsed-rows)))))))
