(ns peoplecli.reporter
  (:require [clj-time.format :as tfm]
            [clj-time.core :as tm]
            [clojure.pprint :as pp]
            [clojure.string :as str]))

;; Sort comparators for pre-defined reports

(defn comp-females-first-then-last-asc
  "a compare function sorting females first then by
   ascending last name, case-insensitively"
  [[l1 _ _ g1 _] [l2 _ _ g2 _]]
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
  [[_ _ dob1 _ _] [_ _ dob2 _ _]]
  (tm/before? dob1 dob2))

#_(-main "resources/pipes.csv" "resources/commas.csv")

#_(-main "-oD" "-sg" "resources/pipes.csv" #_"resources/commas.csv")

(defn people-report [col-specs people-data]
  (doseq [[title comparator]
          [["By Gender and Last Name" comp-females-first-then-last-asc]
           ["By DOB" comp-dob-asc]
           ["By Descending Last Name" comp-last-dsc]
           ]]
    ;;
    ;; --- title and column headers --------------------------------
    ;;
    (pp/cl-format true
      "~&~%~%~a~%------------------------~%" title)

    (pp/cl-format true "~&~%")
    (doseq [spec col-specs]
      (pp/cl-format true
        (:format-field spec)
        (:label spec)))

    (pp/cl-format true "~&~%")
    (doseq [spec col-specs]
      (pp/cl-format true
        (:format-field spec)
        (apply str (repeat (count (:label spec)) "-"))))

    (doseq [person-vals (sort comparator people-data)]
      ;;
      ;; --- the people --------------------------------
      ;;
      (pp/cl-format true "~%")
      (doall (map (fn [spec val]
             (pp/cl-format true
               (:format-field spec)
               (try
                 ((or (:formatter spec) identity) val)
                 (catch Exception e
                   "#####"))))
        col-specs person-vals)))
    (pp/cl-format true "~%")))