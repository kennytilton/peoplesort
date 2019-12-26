(ns peoplesort.handler
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults]]
            [ring.middleware.json :refer [wrap-json-response]]
            [peoplesort.upload :as upl]
            [peoplesort.output :as out]
            [peoplesort.http :as http]))

; Our spec:
;   POST /records - Post a single data line in any of the 3 formats supported by your existing code
;   GET /records/gender - returns records sorted by gender
;   GET /records/birthdate - returns records sorted by birthdate
;   GET /records/name - returns records sorted by name

(defroutes app-routes
  (context "/records" []
    (POST "/" req
      (upl/person-add-one req))
    ;; adds an array of persons:
    (POST "/bulk" req
      (upl/person-add-bulk req))
    ;; returns count of persons stored:
    (GET "/count" req
      (out/people-count req))
    ;; clears datastore:
    (POST "/reset" req
      (upl/people-reset! req))
    ;; returns all persons sorted as requested:
    (GET "/orderedby" req
      (out/stored-persons-ordered-by req))
    ;; returns all persons according to hardcoded sorts...
    (GET "/gender" req
      (out/stored-persons-by-gender req))
    (GET "/name" req
      (out/stored-persons-by-name req))
    (GET "/birthdate" req
      (out/stored-persons-by-birthdate req)))
  (route/not-found "Invalid route toplevel."))

(def app
  (wrap-json-response
    (wrap-defaults app-routes http/unsecure-site-defaults)))

