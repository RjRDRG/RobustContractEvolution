if(status == #STATUS) {
    HttpHeaders headers = new HttpHeaders();
    #HEADERS

    String body;
    #BODY

    return ResponseEntity.ok().headers(headers).body(body);
}
