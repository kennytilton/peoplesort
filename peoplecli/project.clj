(defproject peoplecli "0.1.0"
  :description "Parse, merge, sort, and display multiple CSVs describing people."
  :url "http://tiltontec.com"
  :license {:name "The MIT License (MIT)"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.2.385"]
                 [com.taoensso/timbre "4.3.1"]
                 [org.clojure/tools.cli "0.3.5"]
                 [clj-time "0.15.2"]]
  :main ^:skip-aot peoplecli.core
  :target-path "target/%s"
  :bin {:name "peoplecli"
        :bin-path "./bin"}
  :profiles {:uberjar {:aot :all}})
