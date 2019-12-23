(ns peoplecli.reporter
  (:require [clj-time.format :as tfm]
            [clj-time.core :as tm]
            [clojure.pprint :as pp]
            [clojure.string :as str]))

;; Sort comparators for pre-defined reports

(defn comp-females-first-then-last-asc
  "a compare function sorting females first then by
   ascending last name, case-insensitively"
  [[l1 _ g1 _ _] [l2 _ g2 _ _]]
  (cond
    (= g1 "female") (or (= g2 "male")
                      ;; both female, use last
                      (compare (str/lower-case l1) (str/lower-case l2)))
    :default
    ;; g1 is male
    (cond
      (= g2 "female") false
      ;; both male, use last
      :default (compare (str/lower-case l1) (str/lower-case l2)))))

(defn comp-last-dsc
  [[l1 _ _ _ _] [l2 _ _ _ _]]
  (compare (str/lower-case l2) (str/lower-case l1)))

(defn comp-dob-asc
  [[_ _ _ _ dob1] [_ _ _ _ dob2]]
  (tm/before? dob1 dob2))

#_(-main "resources/pipes.csv" "resources/commas.csv")

(defn dob-display [dob]
  "Convert Date object to mm/dd/YYYY"
  (tfm/unparse (tfm/formatter "MM/dd/yyyy") dob))

#_(-main "-oD" "-sg" "resources/pipes.csv" #_"resources/commas.csv")

(defn people-report [people-data]
  (let [col-formatters [nil nil nil nil dob-display]]
    (doseq [[title comparator]
            [["By Gender and Last Name" comp-females-first-then-last-asc]
             ["By DOB" comp-dob-asc]
             ["By Descending Last Name" comp-last-dsc]]]
      (pp/cl-format true "~&~%~%~a~%------------------------~%" title)
      (pp/cl-format true "~&~20a ~20a ~10a ~20a ~10a~%"
        "First" "Last" "Gender" "Favorite Color" "DOB")
      (doseq [person-vals (sort comparator people-data)]
        (let [[last first gender color dob]
              (map (fn [person-val formatter]
                     (try
                       ((or formatter identity) person-val)
                       (catch Exception e
                         "#####")))
                person-vals col-formatters)]
          (pp/cl-format true "~&~20a ~20a ~10a ~20a ~10a~%"
            first last gender color dob))))))