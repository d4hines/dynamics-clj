(ns dynamics-clj.core
  (:require [cheshire.core :refer [generate-string]]
            [clj-http.client :as client]
            [clojure.pprint :refer [pprint]]
            [clojure.string :as str]
            [ring.util.codec :refer [url-encode]]
            [cheshire.core :refer [generate-string]]))

(def config {:crmorg "https://d4hines.crm.dynamics.com"
             :clientid "The id of the native app you registered in azure"
             :username "username"
             :password "password"
             :tokenendpoint "https://login.microsoftonline.com/yourtkenendpoint/oauth2/token"
             :crmwebapipath "/api/data/v9.0/"}) ;; replace v9.0 as appropriate

(defn error-fn [exception] (.getMessage exception))

(defn get-token
  "Retrieves an OAuth2 token from Azure Active Directory.
  Uses username and password credentials. Passes retrieved token to `callback`"
  [{:keys [crmorg clientid username password tokenendpoint]}
   callback]
  (let [reqstring (str "client_id=" clientid
                       "&resource=" (url-encode crmorg)
                       "&username=" (url-encode username)
                       "&password=" (url-encode password)
                       "&grant_type=password")
        options {:body reqstring
                 :headers {"Content-Type" "application/x-www-form-urlencoded"}
                 :as :json
                 :async? true}]
    (client/post tokenendpoint
                 options
                  ;; respond callback
                 (fn [response] (callback (get-in response [:body :access_token])))
                  ;; raise callback
                 error-fn)))

(def crm-options
  "Defaults for clj-http requests, along with the headers needed for the CRM OData requests."
  {:async? true
   :as :json
   :headers {"OData-MaxVersion" "4.0",
             "OData-Version" "4.0",
             "Accept" "application/json",
             "Content-Type" "application/json; charset=utf-8",
             "Prefer" "odata.maxpagesize=500,odata.include-annotations=OData.Community.Display.V1.FormattedValue"}})

(def api-url (str (:crmorg config) (:crmwebapipath config)))

(defn concat-fields [fields] (if (-> fields count (= 1)) (first fields) (str/join "," fields)))

(defn retrieve [entity-col id fields callback]
  "Retrieves a single entity from CRM.
  `entity-col` is the entity's logical collection name (the plural, lowercase).
  `id` is the entity's GUID.
  `fields` is a coll. of logical field names.
  `callback` is a function that accepts one argument which is the a map representing the retrieved entity"
  (get-token config
             #(client/get
               (str api-url entity-col "(" id ")?$select=" (concat-fields fields))
               (assoc crm-options :oauth-token %)
               (fn [response] (callback (get-in response [:body])))
               (fn [exception] (println "Error retrieving OAuth token: ")))))

(retrieve "contacts" "914b2297-bf2f-e811-a833-000d3a33b3a3" ["fullname","createdon"] pprint)

(defn retrieve-multiple [entity-col fields filter callback]
  "Retrieves a single entity from CRM.
  `entity-col` is the entity's logical collection name (the plural, lowercase).
  `fields` is a coll. of logical field names.
  `filter` is an OData filter string, Pass nil if not required.
  `callback` is a function that accepts one argument which is the an array representing the retrieved entities."
  (get-token config
             #(client/get (str api-url entity-col
                               "?$select=" (concat-fields fields)
                               (if filter (str "&$filter=" filter) ""))
                          (assoc crm-options :oauth-token %)
                          (fn [response] (callback (get-in response [:body :value])))
                          error-fn)))

(retrieve-multiple "contacts" ["fullname","createdon"] nil pprint)

(defn create-record [entity-col new-record callback]
  "Creates a record in CRM, returning the new id.
  `entity-col` is the entity's logical collection name (the plural, lowercase).
  `new-record` is a map of the fields to be created on the new entity record
  `callback` is a function that accepts one argument which is the id of the new record as a string."
  (get-token config
             #(client/post (str api-url entity-col)
                           (assoc crm-options :content-type :json
                                  :oauth-token %
                                  :body (generate-string new-record))
                           (fn [response] (let [url (get-in response [:headers "OData-EntityId"])
                                                id (second (str/split url #"\(|\)" ))]
                                            (callback id)))
                           error-fn)))

(create-record "contacts" {:firstname "test6" :lastname "person6"} pprint)

(defn update-record [entity-col id updated-record callback]
  "Creates a record in CRM, returning the new id.
  `entity-col` is the entity's logical collection name (the plural, lowercase).
  `update-record` is a map of the fields/values to be updated on the new entity record.
  `callback` is a function of zero arguments called on succes."
  (get-token config
             #(client/patch (str api-url entity-col "(" id ")")
                           (assoc crm-options :content-type :json
                                  :oauth-token %
                                  :body (generate-string update-record))
                           callback
                           error-fn)))

(update-record "contacts" {:firstname "test6" :lastname "person6"} pprint)

(client/post "https://postman-echo.com/post"
             {:body (generate-string {:json "input"})
              :headers {"X-Api-Version" "2"}
              :content-type :json
              :socket-timeout 1000  ;; in milliseconds
              :conn-timeout 1000    ;; in milliseconds
              :as :json})

(comment
  ;; Should return record
  (retrieve-multiple "contacts" ["fullname","createdon"] "firstname eq 'Billy'" pprint)
  ;; Should return empty arr
  (retrieve-multiple "contacts" ["fullname","createdon"] "firstname eq 'not exist'" pprint))

