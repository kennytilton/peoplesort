(ns peoplecli.reporter
  (:require [clj-time.format :as tfm]
            [clj-time.core :as tm]
            [clojure.pprint :as pp]
            [clojure.string :as str]))

;;;; --- Sort comparators for pre-defined reports
;; --- Last descending, case insensitively ----
(defn comp-last-dsc
  [[l1 _ _ _ _] [l2 _ _ _ _]]
  (compare (str/lower-case l2) (str/lower-case l1)))

;; --- Gender/Last -----------------------
(defn gender? [g]
  (some #{g} ["male" "female"]))

(defn comp-females-first-then-last-asc
  "a compare function sorting females first then by
   ascending last name, case-insensitively"
  [[l1 _ _ g1 _] [l2 _ _ g2 _]]
  (if (gender? g1)
    (if (gender? g2)
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
    (if (gender? g2)
      false
      true)))

;; --- DOB ascending ------------------------
(defn date-time? [x]
  (instance? org.joda.time.DateTime x))

(defn comp-dob-asc
  "Comparator for ascending dates, dropping invalid dates to bottom"
  [[_ _ dob1 _ _] [_ _ dob2 _ _]]
  (if (date-time? dob1)
    (if (date-time? dob2)
      (tm/before? dob1 dob2)
      true)
    (if (date-time? dob2)
      false
      true)))

;; --- the report ---------------------------

(defn people-report-header
  "Output title and column headers"
  [title col-specs]

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
      (apply str (repeat (count (:label spec)) "-")))))

(defn people-report [col-specs people-data]
  (doseq [[title comparator]
          [["By Gender and Last Name" comp-females-first-then-last-asc]
           ["By DOB" comp-dob-asc]
           ["By Descending Last Name" comp-last-dsc]]]
    (people-report-header title col-specs)
    ;;
    ;; --- the people --------------------------------
    ;;
    (doseq [person-vals (sort comparator people-data)]
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