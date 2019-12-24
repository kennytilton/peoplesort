(ns peoplesort.output
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [clojure.data.json :as json]
            [ring.util.response :refer [response]]
            [peoplesort.utility
             :refer [people-store
                     CORS-HEADERS
                     build-resp
                     usage-error]]))

(defn people-count [req]
  (try
    (build-resp 200 {:count (count @people-store)})
    (catch Exception e
      {:status  500
       :headers (merge CORS-HEADERS
                  {"Content-Type" "text/html"})
       :body    "<h1>Something 500 happened.</h1>"})))

(defn stored-persons-by-dob [req]
  (try
    (build-resp 200
      @people-store)
    (catch Exception e
      {:status  500
       :headers (merge CORS-HEADERS
                  {"Content-Type" "text/html"})
       :body    "Retrieve persons by name failed."})))