(ns peoplesort.base
  (:require
    [clojure.pprint :as pp]
    [clj-time.core :as tm]
    [clj-time.format :as tfm]
    [peoplesort.sorting :as sort]))

(defn pprt
  ([x] (pprt :anon x))
  ([tag x] (pp/pprint [tag x])))

(defn dob-parse
  "Convert from YYYY-mm-dd to Date object"
  [dob-in]
  (tfm/parse (tfm/formatter "yyyy-MM-dd") dob-in))

(defn dob-display
  "Convert Date object to mm/dd/YYYY. Keyword :error signifies
  data already determined to be invalid"
  [dob]
  (case dob
    :error "#####"
    (tfm/unparse (tfm/formatter "MM/dd/yyyy") dob)))

(def person-properties
  "Specifications of each delimited property, in the order required."
  [{:name :LastName}
   {:name :FirstName}
   {:name       :Gender
    :parser     (fn [g]
                  (or (some #{g} ["male" "female"])
                    (throw (Exception. (str "Invalid gender: " g)))))
    :comparator (sort/compare-unary #(= % "female"))}
   {:name :FavoriteColor}
   {:name       :DateOfBirth
    :parser     dob-parse
    :formatter  dob-display
    :comparator (sort/compare-binary tm/before?)}])

(def person-property
  "A dictionary of properties keyed by name"
  (into {}
    (for [{:keys [name] :as prop} person-properties]
      [name prop])))