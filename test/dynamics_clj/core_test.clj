(ns dynamics-clj.core-test
  (:require [clojure.test :refer :all]
            [dynamics-clj.core :refer :all])
  (:use clj-http.fake))

(def config { ;; Azure AD Username
             :username "user@myorg.onmicrosoft.com"
             ;; Azure AD password
             :password "secretpassword"
             ;; Azure Active Directory > App Registrations > Endpoints > OAuth Token Endpoint
             :tokenendpoint "https://login.microsoftonline.com/some-guid-here/oauth2/token"
             ;; Dynamics CRM Url
             :crmorg "https://myorg.crm.dynamics.com"
             ;; You must register a new app in Azure AD > App Registrations. The type must be "Native".
             ;; This will generate a new application with an application id (synonym for clientid) 
             ;; You must then go to Settings > Required Permissions (scroll right) > click Add > Dynamics CRM Online > click "Grant Permissions" (required!)
             :clientid  "some-guid"
             ;; Dynamics CRM > Settings > Customizations > Developer Settings > Web API Url (2016)
             :crmwebapipath "https://myorg.crm.dynamics.com/api/data/v9.0/"})

(def multi-response "{\"@odata.context\":\"https://myorg.crm.dynamics.com/api/data/v9.0/$metadata#contacts(fullname)\",\"value\":[{\"fullname\":\"Billy Bob\",\"contactid\":\"95cb1af9-3934-e811-a834-000d3a33b1e4\"}]}")

  (def single-response "{\"@odata.context\":\"https://d4hines.crm.dynamics.com/api/data/v9.0/$metadata#contacts(fullname)/$entity\",\"fullname\":\"Billy Bob\",\"contactid\":\"e2577e87-3a34-e811-a834-000d3a33b1e4\"}")

  (def token "secrettoken")
(with-fake-routes {
             })
