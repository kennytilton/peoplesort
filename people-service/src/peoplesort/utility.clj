(ns peoplesort.utility
  (:require
    [clojure.data.json :as json]
    [clojure.pprint :as pp]))

(def unsecure-site-defaults
  {:params    {:urlencoded true
               :multipart  true
               :nested     true
               :keywordize true}
   :cookies   true
   :session   {:flash true
               :cookie-attrs {:http-only true}}
   :security  {:anti-forgery   false ;; true
               :xss-protection nil ;; {:enable? true, :mode :block}
               :frame-options  nil ;;:sameorigin
               :content-type-options :nosniff}
   :static    {:resources "public"}
   :responses {:not-modified-responses true
               :absolute-redirects     true
               :content-types          true
               :default-charset        "utf-8"}})

(def Response-OK 200)
(def Unprocessable-Entity 422)
(def Not-Found 404)

(defn pprt
  ([x] (pprt :anon x))
  ([tag x] (pp/pprint [tag x])))

(defn body->map [response]
  (json/read-str (:body response) :key-fn keyword))

(def CORS-HEADERS {"Content-Type"                 "application/json"
                   "Access-Control-Allow-Headers" "Content-Type"
                   "Access-Control-Allow-Origin"  "*"})

(defn build-resp [status body-ext]
  {:status  status
   :headers CORS-HEADERS
   :body    body-ext})

(defn usage-error
  ([status msg]
   (usage-error status msg nil))
  ([status msg x-info]
   (build-resp status (merge x-info
                        {:usageError msg}))))

(def people-store (atom nil))
