(defproject peoplesort "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.json "0.2.7"]
                 [compojure "1.5.1"]
                 [cheshire "5.8.0"]
                 [com.cemerick/url "0.1.1"]
                 [ring/ring-defaults "0.2.1"]
                 [ring/ring-json "0.4.0"]]

  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler peoplesort.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}})
