# dynamics-clj
This is a helper library for the Dynamics CRM Web API, handling authentication and providing convenient wrappers for the common tasks. Currently the library only supports Online instances.

## Usage

Dynamics CRM Online uses OAuth authentication. To use this library in your app, you must first register it in Azure Active Directory (the same account that CRM is under). Thr library currently supports only one method of authentication, and requires a certain configuration in AAD. 

Every function takes a map as its first argument containing the credentials necessary to authenticate.
The map should have the following structure (instructions in the comments):
```clojure
  (def config {;; Azure AD Username
              :username "user@myorg.onmicrosoft.com"
              ;; Azure AD password
              :password "secretpassword"
              ;; Azure Active Directory > App Registrations > Endpoints > OAuth Token Endpoint
              :tokenendpoint "https://login.microsoftonline.com/some-guid-here/oauth2/token"
              ;; Dynamics CRM Url
              :crmorg "https://myorg.crm.dynamics.com"
              ;; You must register a new app in Azure AD > App Registrations. The type must be "Native".
              ;; This will generate a new application with an application id (synonym for clientid) 
              ;; You must then go to:
              ;; Settings > Required Permissions (scroll right)
              ;;     > click Add
              ;;     > Dynamics CRM Online
              ;;     > click "Grant Permissions" (required!)
              :clientid  "some-guid"
              ;; Dynamics CRM > Settings > Customizations > Developer Settings > Web API Url (2016)
              :crmwebapipath "https://myorg.crm.dynamics.com/api/data/v9.0/"})

```

For help with the individual functions, see the docstrings for detailed explanations and examples.

## Todo
Pull Requests welcome!

- [ ] Support other Web API functions/actions
- [x] Basic CRUD Operations
- [ ] On-Premise support
- [ ] Bring-your-own-token support
- [x] Online Support

For much more feature-complete implementations in several languages, see [Xrm.Tools.CRMWebAPI](https://github.com/davidyack/Xrm.Tools.CRMWebAPI), by David Yack.

```



               ___           ___           ___                                
              /\  \         /\  \         /\__\      ___                      
             /::\  \       /::\  \       /:/  /     /\  \                     
            /:/\ \  \     /:/\:\  \     /:/  /      \:\  \                    
           _\:\~\ \  \   /:/  \:\  \   /:/  /       /::\__\                   
          /\ \:\ \ \__\ /:/__/ \:\__\ /:/__/     __/:/\/__/                   
          \:\ \:\ \/__/ \:\  \ /:/  / \:\  \    /\/:/  /                      
           \:\ \:\__\    \:\  /:/  /   \:\  \   \::/__/                       
            \:\/:/  /     \:\/:/  /     \:\  \   \:\__\                       
             \::/  /       \::/  /       \:\__\   \/__/                       
              \/__/         \/__/         \/__/                               
                        ___           ___           ___                       
                       /\  \         /\  \         /\  \                      
                      /::\  \       /::\  \       /::\  \                     
                     /:/\:\  \     /:/\:\  \     /:/\:\  \                    
                    /:/  \:\__\   /::\~\:\  \   /:/  \:\  \                   
                   /:/__/ \:|__| /:/\:\ \:\__\ /:/__/ \:\__\                  
                   \:\  \ /:/  / \:\~\:\ \/__/ \:\  \ /:/  /                  
                    \:\  /:/  /   \:\ \:\__\    \:\  /:/  /                   
                     \:\/:/  /     \:\ \/__/     \:\/:/  /                    
                      \::/__/       \:\__\        \::/  /                     
                       ~~            \/__/         \/__/                      
      ___           ___       ___           ___                       ___     
     /\  \         /\__\     /\  \         /\  \          ___        /\  \    
    /::\  \       /:/  /    /::\  \       /::\  \        /\  \      /::\  \   
   /:/\:\  \     /:/  /    /:/\:\  \     /:/\:\  \       \:\  \    /:/\:\  \  
  /:/  \:\  \   /:/  /    /:/  \:\  \   /::\~\:\  \      /::\__\  /::\~\:\  \ 
 /:/__/_\:\__\ /:/__/    /:/__/ \:\__\ /:/\:\ \:\__\  __/:/\/__/ /:/\:\ \:\__\
 \:\  /\ \/__/ \:\  \    \:\  \ /:/  / \/_|::\/:/  / /\/:/  /    \/__\:\/:/  /
  \:\ \:\__\    \:\  \    \:\  /:/  /     |:|::/  /  \::/__/          \::/  / 
   \:\/:/  /     \:\  \    \:\/:/  /      |:|\/__/    \:\__\          /:/  /  
    \::/  /       \:\__\    \::/  /       |:|  |       \/__/         /:/  /   
     \/__/         \/__/     \/__/         \|__|                     \/__/    

```