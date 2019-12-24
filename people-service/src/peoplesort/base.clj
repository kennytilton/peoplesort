(ns peoplesort.base
  (:require
    [clojure.data.json :as json]
    [clojure.pprint :as pp]
    [clj-time.format :as tfm]))

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

(def people-props-reqd
  [{:name :LastName}
   {:name :FirstName}
   {:name :Gender
    :parser (fn [g]
              (or (some #{g} ["male" "female"])
                (throw (Exception. (str "Invalid gender: " g)))))}
   {:name :FavoriteColor}
   {:name :DateOfBirth
    :parser    dob-parse
    :formatter dob-display}])
