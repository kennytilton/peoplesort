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

(defn order-by [rows & order-specs]
  (apply nested-sort rows
    (map #(apply compare-property %) order-specs)))

#_ (order-by
     [{:LastName "Turner", :FirstName "Ted", :Gender "male", :FavoriteColor "gray", :DateOfBirth "11/19/1938"}
      {:LastName "Turner", :FirstName "Bachman", :Gender "male", :FavoriteColor "various", :DateOfBirth "06/30/1973"}
      {:LastName "Turner", :FirstName "Tina", :Gender "female", :FavoriteColor "saphireBlue", :DateOfBirth "11/26/1939"}
      {:LastName "Lama", :FirstName "Dalai", :Gender "male", :FavoriteColor "saffron", :DateOfBirth "07/06/1935"}
      {:LastName "BeebleBrox", :FirstName "Zaphod", :Gender "male", :FavoriteColor "gold", :DateOfBirth "01/19/2098"}
      {:LastName "Smith", :FirstName "Bob", :Gender "male", :FavoriteColor "green", :DateOfBirth "08/23/2011"}]
     [:LastName :asc] [:FirstName :asc])