(ns peoplesort.upload
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [clojure.data.json :as json]
            [ring.util.response :refer [response]]
            [peoplesort.utility
             :refer [people-store
                     CORS-HEADERS
                     build-resp
                     usage-error]]))

(defn people-reset
  "Think of this as uploading the empty set"
  [req]
  (try
    (do
      (reset! people-store nil)
      ;; take honest count!
      (build-resp 200 {:new-count (count @people-store)}))
    (catch Exception e
      (build-resp 500 {:appfail "unknown"}))))

(defn person-add-one [req]
  (try
    (let [{:keys [raw]} (:params req)]
      (cond
        (some str/blank? [raw])
        (usage-error 402 "Person data blank. Ignored.")

        :default
        (build-resp 200 {:new-count
                         (count
                           (swap! people-store conj raw))})))

    ;; todo log/report error usefully
    (catch Exception e
      {:status  500
       :headers (merge CORS-HEADERS
                  {"Content-Type" "text/html"})
       :body    "Add person failed."})))

(defn person-add-bulk [req]
  (try
    (let [{:keys [persons]} (:params req)]
      (cond
        (some str/blank? [persons])
        (usage-error 402 "Person data blank. Ignored.")

        :default
        (build-resp 200 {:new-count
                         (count
                           (swap! people-store concat persons))})))

    (catch Exception e
      {:status  500
       :headers (merge CORS-HEADERS
                  {"Content-Type" "text/html"})
       :body    "Add person failed."})))
