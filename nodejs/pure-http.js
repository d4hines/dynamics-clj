// oauth token endpoint: https://login.microsoftonline.com/94348c85-35ff-4ed8-aecd-61b85d0f4c17/oauth2/token
'use strict';
process.env.https_proxy = "http://127.0.0.1:8888";
process.env.http_proxy = "http://127.0.0.1:8888";
process.env.NODE_TLS_REJECT_UNAUTHORIZED = "0";
var https = require('https');
console.log('pure http test')
//set these values to retrieve the oauth token
var crmorg = 'https://d4hines.crm.dynamics.com';
var clientid = '020a1b54-a7d4-4732-8efc-01c13adf8c0c';
var username = 'd4hines@d4hines.onmicrosoft.com';
var userpassword = 'yqW4FyhE9TsQcA6O';
var tokenendpoint = 'https://login.microsoftonline.com/94348c85-35ff-4ed8-aecd-61b85d0f4c17/oauth2/token';
 
//set these values to query your crm data
var crmwebapihost = 'd4hines.api.crm.dynamics.com';
var crmwebapipath = '/api/data/v9.0/contacts'; //basic query to select contacts
 
//remove https from tokenendpoint url
tokenendpoint = tokenendpoint.toLowerCase().replace('https://','');
 
//get the authorization endpoint host name
var authhost = tokenendpoint.split('/')[0];

//get the authorization endpoint path
var authpath = '/' + tokenendpoint.split('/').slice(1).join('/');
 
//build the authorization request
//if you want to learn more about how tokens work, see IETF RFC 6749 - https://tools.ietf.org/html/rfc6749
var reqstring = 'client_id='+clientid;
reqstring+='&resource='+encodeURIComponent(crmorg);
reqstring+='&username='+encodeURIComponent(username);
reqstring+='&password='+encodeURIComponent(userpassword);
reqstring+='&grant_type=password';

//set the token request parameters
var tokenrequestoptions = {
    host: authhost,
    path: authpath,
    method: 'POST',
    headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'Content-Length': Buffer.byteLength(reqstring)
    }
};
var start = new Date();
console.log(start);
//make the token request
var tokenrequest = https.request(tokenrequestoptions, function(response) {
    //make an array to hold the response parts if we get multiple parts
    var responseparts = [];
    response.setEncoding('utf8');
    response.on('data', function(chunk) {
        //add each response chunk to the responseparts array for later
        responseparts.push(chunk);		
    });
    response.on('end', function(){
        //once we have all the response parts, concatenate the parts into a single string
        var completeresponse = responseparts.join('');
        //console.log('Response: ' + completeresponse);
        console.log('Token response retrieved . . . ');
        
        //parse the response JSON
        var tokenresponse = JSON.parse(completeresponse);
        
        //extract the token
        var token = tokenresponse.access_token;
        console.log(new Date());
        //pass the token to our data retrieval function
        getData(token);
    });
});
tokenrequest.on('error', function(e) {
    console.error(e);
});
 
//post the token request data
tokenrequest.write(reqstring);
 
//close the token request
tokenrequest.end();
 
 
function getData(token){
    //set the web api request headers
    var requestheaders = { 
        'Authorization': 'Bearer ' + token,
        'OData-MaxVersion': '4.0',
        'OData-Version': '4.0',
        'Accept': 'application/json',
        'Content-Type': 'application/json; charset=utf-8',
        'Prefer': 'odata.maxpagesize=500',
        'Prefer': 'odata.include-annotations=OData.Community.Display.V1.FormattedValue'
    };
    
    //set the crm request parameters
    var crmrequestoptions = {
        host: crmwebapihost,
        path: crmwebapipath,
        method: 'GET',
        headers: requestheaders
    };
    
    //make the web api request
    var crmrequest = https.request(crmrequestoptions, function(response) {
        //make an array to hold the response parts if we get multiple parts
        var responseparts = [];
        response.setEncoding('utf8');
        response.on('data', function(chunk) {
            //add each response chunk to the responseparts array for later
            responseparts.push(chunk);		
        });
        response.on('end', function(){
            console.log(new Date());
            //once we have all the response parts, concatenate the parts into a single string
            var completeresponse = responseparts.join('');
            
            //parse the response JSON
            var collection = JSON.parse(completeresponse).value;
            
            //loop through the results and write out the fullname
            collection.forEach(function (row, i) {
                console.log(row);
            });
        });
    });
    crmrequest.on('error', function(e) {
        console.error(e);
    });
    //close the web api request
    crmrequest.end();
}
