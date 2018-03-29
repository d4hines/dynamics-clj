// Dynamics crm
// ApplicationID: 00000007-0000-0000-c000-000000000000

// dynamics-clj-02
// Secret: test-key: 00LzX9xN5wTQMKGhJLapYa+W9pI2Qf8s/0239VFgkJE=
// ApplicationID: db1d57aa-442c-4878-be22-b08ca8f7a97c
// Reply URL: http://localhost
// AD Tenant ID: 94348c85-35ff-4ed8-aecd-61b85d0f4c17
// TYpe: Web API

// test4
// Application id: 020a1b54-a7d4-4732-8efc-01c13adf8c0c
// Reply URL: http://localhost



console.log('Pure ADAL test')
var AuthenticationContext = require('adal-node').AuthenticationContext;
 
var authorityHostUrl = 'https://login.windows.net';
var tenant = 'd4hines.onmicrosoft.com'; // AAD Tenant name.
var authorityUrl = authorityHostUrl + '/' + tenant;
var applicationId = '020a1b54-a7d4-4732-8efc-01c13adf8c0c'; // Application Id of app registered under AAD.
var resource = 'https://d4hines.crm.dynamics.com'; // URI that identifies the resource for which the token is valid.
var username = 'd4hines@d4hines.onmicrosoft.com';
var password = 'YechiGlihyechu8';
var context = new AuthenticationContext(authorityUrl);
 
function callback (err, tokenResponse) {
  if (err) {
    console.log('well that didn\'t work: ' + err.stack);
  } else {
    console.log(JSON.stringify(tokenResponse));
  }
}

context.acquireTokenWithUsernamePassword(resource, username, password, applicationId, callback);
