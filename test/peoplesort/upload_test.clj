(ns peoplesort.upload-test
  (:require [clojure.test :refer :all]
            [peoplesort.core :refer [app]]
            [peoplesort.properties :refer :all]
            [peoplesort.http :refer :all]
            [peoplesort.test-utility :refer :all]
            [clojure.string :as str]))

(deftest unhappy-paths
  (testing "Blank"
    (is-unprocessable
      (postperson "")))
  (testing "Bad delimiter"
    (is-unprocessable
      (postperson
        "Turner # Tina | female | saphireBlue | 1939-11-26")))
  (testing "Bad column counts"
    (is-unprocessable
      (postperson
        "Turner Tina | female | saphireBlue | 1939-11-26"))
    (is-unprocessable
      (postperson
        "Turner | Tina | female | saphire | Blue | 1939-11-26"))))

(deftest parse-fail-to-hashes
  ;; Make sure invalid property does not cause whole row to be rejected
  (testing "Row is accepted despite bad birth date"
    (rqreset!)
    (is-body-count 1 (postperson
                       "Turner Tina female saphireBlue 1939-111-26"))

    (let [response (rqget "/records/name")]
      (is-response-ok response)
      (let [bd (:DateOfBirth (first (response-body->map response)))]
        (is (and (not (str/blank? bd))
              (every? #(= \# %) bd))))))

  (testing "Row is accepted despite bad gender"
    (rqreset!)
    (is-body-count 1 (postperson
                       "Turner Tina invalid saphireBlue 1939-11-26"))

    (let [response (rqget "/records/name")]
      (is-response-ok response)
      (let [g (:Gender (first (response-body->map response)))]
        (is (and (not (str/blank? g))
              (every? #(= \# %) g)))))))

(deftest happy-path-store-and-retrieve
  (testing
    (is-body-count 0 (rqpost "/records/reset"))
    (is-body-count 1 (postperson
                       "Smith | Bob | male | green | 2011-08-23"))

    (let [response (rqget "/records/name")]
      (is-response-ok response)
      (is (= (response-body->map response)
            [{:LastName      "Smith", :FirstName "Bob", :Gender "male",
              :FavoriteColor "green",
              :DateOfBirth   "08/23/2011"}])))

    (is-body-count 2 (postperson
                       "BeebleBrox Zaphodra male gold 2098-1-19"))

    (is-body-count 4 (postpersons
                       "Lama, Dalai, male, saffron, 1935-7-6"
                       "Turner | Tina | female | saphireBlue | 1939-11-26"))

    (let [response (rqget "/records/count")]
      (is-response-ok response)
      (is (= (response-body->map response)
            {:count 4})))

    (let [response (rqget "/records/name")]
      (is-response-ok response)
      (is (= (map :LastName (response-body->map response))
            ["BeebleBrox" "Lama" "Smith" "Turner"])))

    (let [response (rqpost "/records/reset")]
      (is-response-ok response)
      (is (= (response-body->map response)
            {:new-count 0})))))
