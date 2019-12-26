(ns peoplesort.persistence
  (:require [peoplesort.sorting :refer :all]))

(def people-datastore
  "Persistence for duration of server run, anyway."
  (atom nil))

(defn store-reset! []
  (reset! people-datastore nil))

(defn record-count []
  (count @people-datastore))

(defn contents []
  @people-datastore)

(defn write! [record]
  (swap! people-datastore conj record))

(defn write-bulk! [records]
  (swap! people-datastore concat records))