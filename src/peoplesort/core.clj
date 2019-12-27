(ns peoplesort.core
  (:require
    [org.httpkit.server :as server]
    [compojure.core :refer :all]
    [compojure.handler :as handler]
    [compojure.route :as route]
    [ring.middleware.defaults :refer [wrap-defaults]]
    [ring.middleware.json :refer [wrap-json-response]]
    [peoplesort.upload :as upl]
    [peoplesort.output :as out]
    [clojure.tools.cli :refer [parse-opts]]
    [peoplesort.http :as http]
    [peoplesort.cla.ingester :as ing]
    [peoplesort.cla.reporter :as rpt])
  (:gen-class))

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
    ;; Next returns all persons sorted as specified by an order-by DSL
    ;; inspired by SQL.
    ;; This because, looking at different sorts specified in the
    ;; exercise, it struck me we do not want to be forever creating
    ;; new endpoints to handle new property/direction permutations.
    (GET "/orderedby" [] out/stored-persons-ordered-by)

    ;; next three return all persons according to hardcoded sorts...
    (GET "/gender" [] out/stored-persons-by-gender)
    (GET "/name" [] out/stored-persons-by-name)
    (GET "/birthdate" [] out/stored-persons-by-birthdate))
  (route/not-found "Invalid route toplevel."))

(def app
  (wrap-json-response
    (wrap-defaults app-routes http/unsecure-site-defaults)))

(def people-cli
  [["-h" "--help"]])

(defn -main [& args]
  (let [input (parse-opts args people-cli)
        {:keys [options arguments summary errors]} input
        {:keys [help]} options
        filepaths arguments
        port (Integer/parseInt (or (System/getenv "PORT") "3000"))]
    (cond
      errors (doseq [e errors]
               (println e))

      help (println "\nUsage:\n    peoplesort options* files*\n\n"
             "If help is requested, we print this to console and exit.\n\n"
             "If files are provided, we parse, merge, report to console, and exit.\n\n"
             "Files must have, in any order, an initial row with these colun headers:\n\n"
             "  LastName | FirstName | Gender | FavoriteColor | DateOfBirth\n\n"
             "Columns may be delimited with pipes, as shown, or commas or spaces. The data\n"
             "may not include delimiters.\n\n"
             "If no files are provided, a service will be started on PORT or 3000.\n\n"
             "Options:\n" (subs summary 1)
             "\n")

      (empty? filepaths)
      ; Start the service
      (server/run-server
        (wrap-json-response
          (wrap-defaults #'app-routes http/unsecure-site-defaults) {:port port})
        ; Run the server without ring defaults
        ;(server/run-server #'app-routes {:port port})
        (println (str "Running webserver at http:/127.0.0.1:" port "/")))

      (not-every? ing/file-found? filepaths)
      (do)

      :default
      (ing/ingest-files-and-report
        filepaths
        rpt/people-report))))