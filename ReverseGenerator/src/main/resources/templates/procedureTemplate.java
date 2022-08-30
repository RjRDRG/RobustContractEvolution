@RequestMapping(
        value = #OLD_PATH#,
        method = #OLD_METHOD#
)
public ResponseEntity<String> #PROCEDURE#(@PathVariable Map<String, String> _pathParams,
                                         @RequestParam Map<String,String> _queryParams,
                                         @RequestHeader Map<String, String> _headerParams,
                                         @RequestBody String _rawBody) {
    try {
        JsonNode _body = mapper.readTree(_rawBody);

        String scheme = #SCHEME#;

        HttpMethod method = HttpMethod.valueOf(#METHOD#);

        String path = #PATH#;

        Map<String, String> pathParams = new HashMap<>();
        #PATH_PARAMS#

        Map<String,String> queryParams = new HashMap<>();
        #QUERY_PARAMS#

        Map<String, String> headerParams = new HashMap<>();
        #HEADER_PARAMS#

        String body = #BODY#;

        MediaType sendType = MediaType.valueOf(#SEND_TYPE#);

        MediaType receiveType = MediaType.valueOf(#RECEIVE_TYPE#);

        ResponseEntity<String> responseEntity = forwardRequest(
            scheme, HOST, PORT, method, path, pathParams, queryParams, headerParams, body, sendType, receiveType
        );

        HttpStatus status = responseEntity.getStatusCode();
        _headerParams = responseEntity.getHeaders().toSingleValueMap();
        _body = mapper.readTree(responseEntity.getBody());

        #RESPONSE#
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.internalServerError().body(e.toString());
    }
}
