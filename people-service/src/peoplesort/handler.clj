(ns peoplesort.handler
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.json :refer [wrap-json-response]]
            [peoplesort.utility :as util]
            [peoplesort.upload :as upl]
            [peoplesort.output :as out]))

;POST /records - Post a single data line in any of the 3 formats supported by your existing code
;GET /records/gender - returns records sorted by gender
;GET /records/birthdate - returns records sorted by birthdate
;GET /records/name - returns records sorted by name

(defroutes app-routes
  (context "/records" []
    (POST "/reset" req
      (upl/people-reset req))
    (POST "/" req
      (upl/person-add-one req))
    (POST "/bulk" req
      (upl/person-add-bulk req))
    (GET "/count" req
      (out/people-count req))
    #_ (GET "/gender" req
      (out/stored-persons-by-gender req))
    #_ (GET "/name" req
      (mgr/stored-persons-by-name req))
    (GET "/birthdate" req
      (out/stored-persons-by-dob req)))
  (route/not-found "Invalid route toplevel."))

(def app
  (wrap-json-response
    (wrap-defaults app-routes util/unsecure-site-defaults)))

