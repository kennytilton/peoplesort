;;;
;;; low-level utilities for handling network requests
;;;
(ns peoplesort.http
  (:require
    [clojure.data.json :as json]))

(def unsecure-site-defaults
  {:params    {:urlencoded true
               :multipart  true
               :nested     true
               :keywordize true}
   :cookies   true
   :session   {:flash        true
               :cookie-attrs {:http-only true}}
   :security  {:anti-forgery         false                  ;; true
               :xss-protection       nil                    ;; {:enable? true, :mode :block}
               :frame-options        nil                    ;;:sameorigin
               :content-type-options :nosniff}
   :static    {:resources "public"}
   :responses {:not-modified-responses true
               :absolute-redirects     true
               :content-types          true
               :default-charset        "utf-8"}})

(def Response-OK 200)
(def Not-Found 404)
(def Unprocessable-Entity 422)

(defn response-body->map [response]
  (when response
    (json/read-str (:body response) :key-fn keyword)))

(def CORS-HEADERS {"Content-Type"                 "application/json"
                   "Access-Control-Allow-Headers" "Content-Type"
                   "Access-Control-Allow-Origin"  "*"})

(defn build-response [status body]
  {:status  status
   :headers CORS-HEADERS
   :body    body})

(defn usage-error
  ([status reason]
   (usage-error status reason nil))
  ([status reason more-info]
   (build-response status (merge more-info
                            {:reason reason}))))

(defn respond-ok [body]
  (println :ok-reponse body)
  (build-response 200 body))

(defn respond-server-fail [e]
  ;; todo: build reason from exception
  (build-response 500 "Server error."))

(defn respond-data-error [reason]
  (build-response Unprocessable-Entity
    {:reason reason}))

(defmacro with-exception-trap [& try-body]
  `(try (do ~@try-body)
        (catch Exception e#
          (respond-server-fail e#))))
