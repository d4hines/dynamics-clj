(ns dynamics-clj.core
  (:require [clojure.string :as str]
            [ring.util.codec :refer [url-encode]]
            [clj-http.client :as client]))

(def config {:crmorg "https://d4hines.crm.dynamics.com"
             :clientid "020a1b54-a7d4-4732-8efc-01c13adf8c0c"
             :username "d4hines@d4hines.onmicrosoft.com"
             :password "YechiGlihyechu8"
             :tokenendpoint "https://login.microsoftonline.com/94348c85-35ff-4ed8-aecd-61b85d0f4c17/oauth2/token"
             :crmwebapihost "d4hines.api.crm.dynamics.com"
             :crmwebapipath "/api/data/v9.0/contacts"})

(def utf-8-octet-length #(-> % (.getBytes "UTF-8") count))

(defn request
  "I don't do a whole lot."
  [{:keys [crmorg clientid username password
           tokenendpoint crmwebapihost crmwebapipath]}]
  (let [split-end (-> tokenendpoint str/lower-case (str/replace #"https:\/\/" "") (str/split #"/"))
        authhost (first split-end)
        authpath (str "/" (->> split-end rest (str/join "/")))
        reqstring (str "client_id=" clientid
                       "&resource=" (url-encode crmorg)
                       "&username=" (url-encode username)
                       "&password=" (url-encode password)
                       "&grant_type=password")
        tokenrequestoptions {:host authhost
                             :path authpath
                             :method "POST"
                             :headers {"Content-Type" "application/x-www-form-urlencoded"
                                        "Content-Length" (utf-8-octet-length reqstring)}}]
    (client/post )))


(request config)
(client/get "http://jsonplaceholder.typicode.com/posts" {:as :json})

(client/post "http://example.com/api"
             {
              :headers {"X-Api-Version" "2"}
              :content-type :json
              :socket-timeout 1000  ;; in milliseconds
              :conn-timeout 1000    ;; in milliseconds
              :accept :json})
