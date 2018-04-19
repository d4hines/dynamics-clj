(ns dynamics-clj.core
  (:require [cheshire.core :refer [generate-string]]
            [clj-http.client :as client]
            [clojure.string :as str]
            [ring.util.codec :refer [url-encode]]
            [slingshot.slingshot :refer [try+]])
  (:import (org.apache.http.client.protocol HttpClientContext)))

(defn get-token
  "Retrieves an OAuth2 token from Azure Active Directory.
  Uses username and password credentials.
  `config` is a map of the configuration needed for OAuth with the CRM. See README for examples."
  [config]
  (let [{:keys [crmorg clientid username password tokenendpoint]} config
        reqstring (str "client_id=" clientid
                       "&resource=" (url-encode crmorg)
                       "&username=" (url-encode username)
                       "&password=" (url-encode password)
                       "&grant_type=password")
        options {:body reqstring
                 :headers {"Content-Type" "application/x-www-form-urlencoded"}
                 :as :json}]
    (get-in (client/post tokenendpoint options) [:body :access_token])))

(def crm-options
  "Defaults for clj-http requests, along with the headers needed for the CRM OData requests."
  {:as :json-string-keys
   :headers {"OData-MaxVersion" "4.0",
              "OData-Version" "4.0",
              "Accept" "application/json",
              "Content-Type" "application/json; charset=utf-8",
              "Prefer" "odata.maxpagesize=500,odata.include-annotations=OData.Community.Display.V1.FormattedValue"}})

(defn retrieve*
  [{:keys [ntlm] :as config} endpoint]
  (let [url (str (:crmwebapipath config) endpoint)]
       (cond
         ntlm
         (let [ctx (HttpClientContext/create)
               [user pass host domain] ntlm]
           (client/with-connection-pool {:threads 1 :default-per-route 1}
             (client/get url {:ntlm-auth [user pass host domain]
                              :http-client-context ctx})
             (client/get url
                         (assoc crm-options :http-client-context ctx))))
        :else
         (client/get url
           (assoc crm-options :oauth-token (get-token config))))))

(defn build-select [fields] (let [field-count (count fields)]
                              (cond (=  0 field-count) nil
                                    (= 1 field-count) (str "$select=" (first fields)))
                              :else (str "$select=" (str/join "," fields))))

(defn retrieve
  "Retrieves a single entity from CRM.
   `config` is a map of the configuration needed for OAuth with the CRM. See README for examples.
  `entity-col` is the entity's logical collection name (the plural, lowercase).
  `id` is the entity's GUID.
  `fields` is a coll. of logical field names. Pass `nil` to include all fields.
  e.g.
  (retrieve \"contacts\" \"914b2297-bf2f-e811-a833-000d3a33b3a3\" [\"fullname\",\"createdon\"])"
  [config entity-col id fields]
  (try+ (:body (retrieve* config (str entity-col "(" id ")"
                                   (if fields (str "?" (build-select fields)) nil))))
       (catch [:status 404] [] {:status 404 :message (str "Id " id " not found in " entity-col)})))

(defn retrieve-multiple
  "Retrieves a single entity from CRM.
  `config` is a map of the configuration needed for OAuth with the CRM. See README for examples.
  `entity-col` is the entity's logical collection name (the plural, lowercase).
  `fields` is an optional coll. of logical field names. Omit if not required.
  `filter` is an optional OData filter string. Pass `nil` to include all fields.
  e.g
  (retrieve-multiple \"contacts\" [\"fullname\",\"createdon\"] \"firstname eq 'bob'\" )"
  [config entity-col fields filter-expr]
  (let [field-str (if fields (build-select fields) nil)
        filter-str (if filter-expr (str "$filter=" filter-expr) nil)
        combined (filter identity [field-str filter-str])
        final-uri (str entity-col
                       (if (> (count combined) 0) (str "?" (str/join "&" combined)) nil))]
    (get-in (retrieve* config final-uri) [:body "value"])))


(defn get-query-by-name [config entity-col query-name userview?]
  (let [query-type (if userview? "user" "saved")
        query-entity (str query-type "queries")
        filter (str "name eq '" query-name "'")
        id-field (str query-type "queryid")
        results (retrieve-multiple config query-entity [id-field] filter)
        cnt (count results)
        id (if (not= cnt 1)
               (throw (Exception. (str "Expected 1 " query-type " view named " query-name
                                   "but found " cnt)))
               (get (first results) id-field))
        final-uri (str entity-col "?" query-type "Query=" id)]
       (get-in (retrieve* config final-uri) [:body "value"])))


(defn create-record
  "Creates a record in CRM, returning the new id.
   `config` is a map of the configuration needed for OAuth with the CRM. See README for examples.
  `entity-col` is the entity's logical collection name (the plural, lowercase).
  `new-record` is a map of the fields to be created on the new entity record
  e.g
  (create-record \"contacts\" {:firstname \"test\" :lastname \"person\"})"
  [config entity-col new-record]
  (let [response (client/post (str (:crmwebapipath config) entity-col)
                              (assoc crm-options :content-type :json
                                     :oauth-token (get-token config)
                                     :body (generate-string new-record)))
        entity-url (get-in response [:headers "OData-EntityId"])]
    (second (str/split entity-url #"\(|\)"))))

(defn update-record
  "Updates an existing CRM record. Returns a ring response map.
  `config` is a map of the configuration needed for OAuth with the CRM. See README for examples.
  `entity-col` is the entity's logical collection name (the plural, lowercase).
  `update-record` is a map of the fields/values to be updated on the new entity record.
  e.g
  (update-record \"contact\" {:firstname \"bob_updated\" :lastname \"person_updated\"})"
  [config entity-col id updated-record]
  (client/patch (str (:crmwebapipath config) entity-col "(" id ")")
                (assoc crm-options :content-type :json
                       :oauth-token (get-token config)
                       :body (generate-string updated-record))))

(defn delete-record
  "Deletes a CRM record. Returns a ring response map.
  `entity-col` is the entity's logical collection name (the plural, lowercase).
  `id` is the entity's GUID.
  e.g
  (delete-record \"contact\" \"9695bd5c-2635-e811-a834-000d3a33b1e4\")"
  [config entity-col id]
  (client/delete (str (:crmwebapipath config) entity-col "(" id ")")
                 (assoc crm-options :content-type :json
                        :oauth-token (get-token config))))

;; EXAMPLES
(comment
  (def config (read-string (slurp "/crm-config.edn")))

  ;; Retrieve a contact, then get the ID of the first one returned.
  (def id (get (first (retrieve-multiple config "contacts" ["fullname"] "firstname eq 'test'"))
               "contactid"))

  ;; Retrieve all fields for a contact
  (retrieve config "contacts" id nil)

  ;; Use retrieve* to target arbitrary endpoints. This is useful for metadata.
  (retrieve* config  "EntityDefinitions(286a3bcb-d539-e811-a833-000d3a33bdbd)?$select=LogicalName&$expand=Attributes($select=LogicalName)"))
