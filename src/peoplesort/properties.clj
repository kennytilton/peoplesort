;;;
;;; definitions and utilities specific to the input properties supported
;;;
(ns peoplesort.properties
  (:require
    [clj-time.core :as tm]
    [clj-time.format :as tfm]
    [peoplesort.sorting :as sort]))

(def parse-fail
  "Just in case someone is named parse-fail..."
  (symbol "parse-fail"))

(defn hashes-iff-error
  ([value]
   (hashes-iff-error value identity))
  ([value formatter]
   "Parsers can emit the symbol parse-fail to handle invalid inputs
   as would a spreadsheet app, so they later get rendered as #####."
   (if (= value parse-fail)
     "#####"
     ((or formatter identity) value))))

(defn dob-parse
  "Convert from YYYY-mm-dd to Date object"
  [dob-in]
  (try
    (tfm/parse (tfm/formatter "yyyy-MM-dd") dob-in)
    (catch Exception e
      parse-fail)))

(defn dob-display
  "Convert Date object to mm/dd/YYYY."
  [dob]
  (tfm/unparse (tfm/formatter "MM/dd/yyyy") dob))

(def person-properties
  "Specifications of each delimited property, in the order required."
  [{:name     :LastName
    :required true}
   {:name :FirstName}
   {:name       :Gender
    :parser     (fn [g]
                  (or (some #{g} ["male" "female"])
                    parse-fail))
    :comparator (sort/compare-unary #(= % "female"))}
   {:name :FavoriteColor}
   {:name       :DateOfBirth
    :parser     dob-parse
    :comparator (sort/compare-binary tm/before?)
    :formatter  dob-display}])

(def person-property
  "A dictionary of properties keyed by name"
  (into {}
    (for [{:keys [name] :as prop} person-properties]
      [name prop])))