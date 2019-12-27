(defproject peoplesort "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://tiltontec.com"
  :min-lein-version "2.0.0"

  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/data.json "0.2.7"]
                 [compojure "1.6.1"]
                 [http-kit "2.3.0"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-json "0.4.0"]
                 [org.clojure/tools.cli "0.3.5"]]


  ;; :plugins [[lein-ring "0.9.7"]]
  ;; :ring {:handler peoplesort.handler/app}
  ;:profiles
  ;{:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
  ;                      [ring/ring-mock "0.3.0"]]}}

  :main ^:skip-aot peoplesort.core
  :target-path "target"
  :bin {:name "peoplesort"
        :bin-path "./bin"}
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                                  [ring/ring-mock "0.3.0"]]}})


