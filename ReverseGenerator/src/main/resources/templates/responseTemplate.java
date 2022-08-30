HttpHeaders responseHeaders = new HttpHeaders();
#RESPONSE_HEADERS#
String responseBody = #RESPONSE_BODY#;
return forwardResponse(#STATUS#, responseHeaders, responseBody);