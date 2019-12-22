(ns peoplecli.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.java.io :as io]
            [taoensso.timbre :as log]
            [clojure.string :as str]
            [clojure.pprint :as pp]
            [clj-time.core :as tm]
            [clj-time.format :as tfm])
  (:gen-class))

(def people-cli

  [["-s" "--sort SORTBY" "Sort by, default by last name."
    :default "n"
    ;; :parse-fn #(Integer/parseInt %)
    :validate [#(str/includes? "nNgGbB" %) "Sort code must be (case insensitively): 'n' for last name, 'g' for gender, or 'b' for birth date."]]

   ["-o" "--order ORDER" "[A]scending or [D]escending order."
    :default 80
    ;;:parse-fn #(Integer/parseInt %)
    :validate [#(str/includes? "aAdD" %) "Must be A or D, case-insensitive, for ascending or descending sort."]]

   ;; A boolean option defaulting to nil
   ["-h" "--help"]]
  )

(declare header-parse file-found?
  people-file-ingest people-file-validate
  dob-parse dob-display)

#_(-main "-sG" "resources/pipes.csv")

(defn -main [& args]
  #_;; uncomment during development so errors get through when async in play
      (Thread/setDefaultUncaughtExceptionHandler
        (reify Thread$UncaughtExceptionHandler
          (uncaughtException [_ thread ex]
            (log/error {:what      :uncaught-exception
                        :exception ex
                        :where     (str "Uncaught exception on" (.getName thread))}))))
  (let [input (parse-opts args people-cli)
        {:keys [options arguments summary errors]} input
        {:keys [help sortby order]} options
        filepaths arguments]

    (cond
      errors (doseq [e errors]
               (println e))

      help (println "\nUsage:\n\n    peoplesort options* files*\n\n"
             "Options:\n" (subs summary 1))

      (empty? filepaths)
      (println "\nNo data files provideed. Exiting.\n\n")

      (not-every? file-found? filepaths)
      (do)

      :default (process-inputs sortby order filepaths))

    ;; WARNING: comment this out for use with REPL
    #_(shutdown-agents)))

(defn process-inputs [sortby order input-files]
  (let [header-parses (map people-file-validate input-files)
        col-ingestors [nil nil nil nil dob-parse]
        col-displayers [nil nil nil nil dob-display]]
    (when (every? seq header-parses)
      (let [parsed-rows (mapcat (partial people-file-ingest col-ingestors)
                          input-files
                          header-parses)
            sorted-rows (sort )
            ordered-rows ((if (some #{order} "aB")
                            identity reverse)
                          sorted-rows)]
        (doseq [row-values ordered-rows]
          (let [[last first gender color dob]
                (map (fn [row-val displayer]
                       (try
                         ((or displayer identity) row-val)
                         (catch Exception e
                           "#####")))
                  row-values col-displayers)]
            (pp/cl-format true "~&~20a ~20a ~10a ~20a ~10a~%"
              last first gender color dob)))))))

#_(-main "-oD" "-sg" "resources/pipes.csv" #_"resources/commas.csv")

(defn dob-parse [dob-in]
  "Convert from YYYY-mm-dd to Date object"
  (tfm/parse (tfm/formatter "yyyy-MM-dd") dob-in))

#_(dob-display (dob-parse "2019-11-23"))

#_(tm/after? (dob-parse "2019-11-23") (dob-parse "2019-10-23"))

(defn dob-display [dob]
  "Convert Date object to mm/dd/YYYY"
  (tfm/unparse (tfm/formatter "MM/dd/yyyy") dob))

#_(-main "-oD" "-sg" "resources/pipes.csv" #_"resources/commas.csv")

(defn people-file-ingest [converters filepath [splitter _]]
  ;(prn :cvs (count converters) :pfi filepath splitter)
  (with-open [rdr (clojure.java.io/reader filepath)]
    (into []
      ;; realize all before closing reader
      (map-indexed (fn [row-no row]
                     ;(prn :mapi row-no row)
                     (let [cols (map str/trim (str/split row splitter))]
                       ;(prn :rpa row-no cols)
                       (when (not= (count cols) (count converters))
                         (throw (Exception. (str "Invalid column count at row "
                                              (inc row-no) " in file " filepath))))
                       (into []
                         (map (fn [col converter]
                                (try
                                  ((or converter identity) col)
                                  (catch Exception e
                                    "#####")))
                           cols converters))))
        (line-seq rdr)))))

(defn people-file-validate [filepath]
  (with-open [rdr (clojure.java.io/reader filepath)]
    (header-parse "|, " 5
      (first (line-seq rdr)))))

(defn header-parse
  "Try to parse string 'header' as a CSV row
  with one of the 'allowed-delims' and check
  that 'req-col-ct' elements are found.

  If OK, return a vector of the regex splitter pattern
  that succeeded and the elements trimmed of whitespace, else nil."
  [allowed-delims reqd-col-ct header]
  (some #(let [splitter (re-pattern (str "\\" %))
               elts (str/split header splitter)]
           (when (= reqd-col-ct (count elts))
             [splitter (mapv str/trim elts)]))
    allowed-delims))

#_(header-parse "|" 2 "a|b")

(defn file-found? [path]
  (or (.exists (io/as-file path))
    (do
      (println (format "\nSuggested file <%s> not found.\n" path))
      false)))

#_(defn print-in-columns [filepaths page-width col-spacing]
    (when-not (empty? filepaths)
      (println :pic)
      (let [
            file-ct (count filepaths)
            col-width (int (Math/floor
                             (/ (- page-width
                                  (* (dec file-ct) col-spacing))
                               file-ct)))
            col-pad (apply str (repeat col-spacing \space))
            filler (apply str (repeat col-width \space))
            channels (for [_ filepaths] (chan))
            futures (doall                                  ;; kick off feeders...
                      (map #(future (column-feeder %1 %2 col-width %3))
                        channels filepaths (range (count filepaths))))]

        ;; ...then pull text:
        (loop []
          (let [chunks (map <!! channels)]
            (cond
              (every? nil? chunks)
              (println "\nThe End\n")

              :default
              (do
                (println (str/join col-pad (map #(or % filler) chunks)))
                (recur))))))))

#_(-main "-t3")

#_(defn column-feeder
    "Return a channel from which an output function
  can pull left-justified column lines of width <col-width>
  extracted from the file at <filepath>."
    [out filepath col-width id]
    (let [filler (apply str (repeat col-width \space))
          rdr (clojure.java.io/reader filepath)]

      (loop [
             ;; valid states:
             ;; :nl - at start of line
             ;; :tx - in the middle of non-whitespace text
             ;; :ws - in the middle of whitespace
             state :nl
             column 0
             last-ws-col nil
             buffer ""]

        (let [pad-out (fn [b]
                        ;; we cannot put to the channel from inside
                        ;; a function (async limitation) so just pad
                        (subs (str b filler) 0 col-width))]
          (cond
            (>= column col-width)                           ;; > should not occur, but...
            (condp = state
              :ws (do
                    (>!! out (pad-out buffer))
                    (recur :nl 0 nil ""))
              :tx (if last-ws-col
                    (do
                      (>!! out (pad-out
                                 (subs buffer 0 (inc last-ws-col))))
                      (let [new-buffer (str/triml (subs buffer last-ws-col))]
                        (recur :tx (count new-buffer) nil new-buffer)))
                    ;; whoa, big word. Hyphenate badly...
                    ;; TODO: hyphenate better
                    (do
                      (>!! out (pad-out (str (subs buffer 0 (dec (count buffer))) \-)))
                      (recur :tx 1 nil (str (last buffer))))))

            :default
            (let [c (.read rdr)]
              (condp = c
                -1 (do
                     (.close rdr)
                     (when (pos? (count buffer))
                       (>!! out (pad-out buffer)))
                     (close! out)
                     id)

                10 (do (>!! out (pad-out buffer))
                       (recur :nl 0 nil ""))

                13                                          ;; maybe on Windows?
                (do (>!! out (pad-out buffer))
                    (recur :nl 0 nil ""))

                9                                           ;; Tabs treated as single space
                (recur :ws (inc column) column (str buffer \space))

                32 (if (= state :nl)
                     (recur :nl 0 nil "")
                     (recur :ws (inc column) column (str buffer \space)))

                (recur :tx (inc column) last-ws-col
                  (str buffer
                    (if (< c 32)
                      \space (char c)))))))))))