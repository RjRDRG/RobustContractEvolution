if(status.value() == #OLD_STATUS#) {
    HttpHeaders responseHeaders = new HttpHeaders();
    #RESPONSE_HEADERS#

    String responseBody = #RESPONSE_BODY#;

    return ResponseEntity.status(#STATUS#).headers(responseHeaders).body(responseBody);
}
