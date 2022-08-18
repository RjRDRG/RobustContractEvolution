if(status == #OLD_STATUS) {
    HttpHeaders responseHeaders = new HttpHeaders();
    #HEADERS

    String responseBody = #BODY

    return ResponseEntity.status(#STATUS).headers(headers).body(body);
}
