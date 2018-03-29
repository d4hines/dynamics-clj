var DynamicsWebApi = require('dynamics-web-api');
var AuthenticationContext = require('adal-node').AuthenticationContext;

//the following settings should be taken from Azure for your application
//and stored in app settings file or in global variables

//OAuth Token Endpoint
var authorityUrl = 'https://login.microsoftonline.com/94348c85-35ff-4ed8-aecd-61b85d0f4c17/oauth2/token';
//CRM Organization URL
var resource = 'https://d4hines.crm.dynamics.com';
//Dynamics 365 Client Id when registered in Azure
var clientId = '00000007-0000-0000-c000-000000000000';
var username = 'd4hines@d4hines.onmicrosoft.com';
var password = 'YechiGlihyechu8';

var adalContext = new AuthenticationContext(authorityUrl);

//add a callback as a parameter for your function
function acquireToken(dynamicsWebApiCallback){
    //a callback for adal-node
    function adalCallback(error, token) {
        if (!error){
            //call DynamicsWebApi callback only when a token has been retrieved
            dynamicsWebApiCallback(token);
        }
        else{
            console.log('Token has not been retrieved. Error: ' + error.stack);
        }
    }

    //call a necessary function in adal-node object to get a token
    adalContext.acquireTokenWithUsernamePassword(resource, username, password, clientId, adalCallback);
}

//create DynamicsWebApi object
var dynamicsWebApi = new DynamicsWebApi({ 
    webApiUrl: 'https:/myorg.api.crm.dynamics.com/api/data/v9.0/',
    onTokenRefresh: acquireToken
});

//call any function
dynamicsWebApi.executeUnboundFunction("WhoAmI").then(function (response) {
    console.log('Hello Dynamics 365! My id is: ' + response.UserId);
}).catch(function(error){
    console.log(error.message);
});