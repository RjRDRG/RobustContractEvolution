@RequestMapping(
        value = #OLD_PATH,
        method = #OLD_METHOD
)
public ResponseEntity<String> #PROCEDURE(@PathVariable Map<String, String> _pathParams,
                                         @RequestParam Map<String,String> _queryParams,
                                         @RequestHeader Map<String, String> _headerParams,
                                         @RequestBody String _body) {
    try {
        JsonNode node = mapper.readTree(body);

        String scheme = #SCHEME;

        String host = #HOST;

        HttpMethod method = HttpMethod.valueOf(#METHOD);

        String path = #PATH;

        Map<String, String> pathParams = new HashMap<>();
        #PATH_PARAMS

        Map<String,String> queryParams = new HashMap<>();
        #QUERY_PARAMS

        Map<String, String> headerParams = new HashMap<>();
        #HEADER_PARAMS

        String body;
        #BODY

        MediaType sendType = MediaType.valueOf(#SEND_TYPE);

        MediaType receiveType = MediaType.valueOf(#RECEIVE_TYPE);

        ResponseEntity<String> responseEntity = Utils.forwardRequest(
                scheme, host, method, path, pathParams, queryParams, headerParams, body, sendType, receiveType
        );

        HttpStatus status = responseEntity.getStatusCode();
        headerParams = responseEntity.getHeaders();
        body = responseEntity.getBody();

        #RESPONSE

    } catch (Exception e) {
        throw new RuntimeException(e);
    }
}
