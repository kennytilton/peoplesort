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
    (POST "/" [] upl/person-add-one)

    ;; --- some extra endpoints that seem handy. ----
    (POST "/bulk" [] upl/persons-add-bulk)
    (GET "/count" [] out/people-count)
    (POST "/reset" [] upl/people-reset!)
    ;; Next returns all persons sorted as specify by an order-by DSL.
    ;; This because, looking at different sorts specified in the
    ;; exercise, it struck me we do not want to be forever creating
    ;; new endpoints to handle new property/direction permutations:
    (GET "/orderedby" [] out/stored-persons-ordered-by)

    ;; next three return all persons according to hardcoded sorts...
    (GET "/gender" [] out/stored-persons-by-gender)
    (GET "/name" [] out/stored-persons-by-name)
    (GET "/birthdate" [] out/stored-persons-by-birthdate))
  (route/not-found "Invalid route toplevel."))

(def app
  (wrap-json-response
    (wrap-defaults app-routes http/unsecure-site-defaults)))

