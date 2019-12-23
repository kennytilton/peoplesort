(ns peoplecli.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.java.io :as io]
            [taoensso.timbre :as log]
            [clojure.string :as str]
            [clojure.pprint :as pp]
            [clj-time.core :as tm]
            [clj-time.format :as tfm]
            [clj-time.coerce :as tc]

            [peoplecli.ingester :as ing]
            [peoplecli.reporter :as rpt])
  (:gen-class))

(def people-cli
  [["-h" "--help"]])

#_(-main "resources/commas.csv")

(defn -main [& args]
  #_;; uncomment during development so errors get through when async in play
      (Thread/setDefaultUncaughtExceptionHandler
        (reify Thread$UncaughtExceptionHandler
          (uncaughtException [_ thread ex]
            (log/error {:what      :uncaught-exception
                        :exception ex
                        :where     (str "Uncaught exception on" (.getName thread))}))))
  (let [input (parse-opts args people-cli)
        {:keys [options arguments summary errors]} input
        {:keys [help]} options
        filepaths arguments]
    (cond
      errors (doseq [e errors]
               (println e))

      help (println "\nUsage:\n\n    peoplesort options* files*\n\n"
             "Options:\n" (subs summary 1))

      (empty? filepaths) (println "\nNo data files provideed. Exiting.\n\n")

      (not-every? ing/file-found? filepaths) (do)

      :default
      (ing/process-inputs
        filepaths
        rpt/people-report
        #_ (fn [_ data] (pp/pprint data)))

      ;; WARNING: comment this out for use with REPL
      #_(shutdown-agents))))