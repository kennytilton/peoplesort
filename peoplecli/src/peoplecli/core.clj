(ns peoplecli.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [peoplecli.ingester :as ing]
            [peoplecli.reporter :as rpt])
  (:gen-class))

(def people-cli
  [["-h" "--help"]])

#_(-main "-h")

#_(-main "resources/commas.csv"
    "resources/pipes.csv"
    "resources/spaces.csv")

(defn -main [& args]
  (let [input (parse-opts args people-cli)
        {:keys [options arguments summary errors]} input
        {:keys [help]} options
        filepaths arguments]
    (cond
      errors (doseq [e errors]
               (println e))

      help (println "\nUsage:\n    peoplesort options* files*\n\n"
             "Options:\n" (subs summary 1)
             "\n")

      (empty? filepaths) (println "\nNo data files provided. Exiting.\n\n")

      (not-every? ing/file-found? filepaths) (do)

      :default
      (ing/ingest-files-and-report
        filepaths
        rpt/people-report))))