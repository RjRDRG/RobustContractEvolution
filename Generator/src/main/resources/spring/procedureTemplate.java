@RequestMapping(
        value = #OLD_PATH,
        method = #OLD_METHOD
)
public ResponseEntity<String> #PROCEDURE(@PathVariable Map<String, String> pathParams,
                                         @RequestParam Map<String,String> queryParams,
                                         @RequestHeader Map<String, String> headerParams,
                                         @RequestBody String body) {
    try {
        JsonNode node = mapper.readTree(body);

        String url = #URL;

        HttpMethod method = #METHOD;

        String path = #PATH;

        Map<String, String> pathParams = new HashMap<>();
        #PATH_PARAMS

        MultiValueMap<String,String> queryParams = new HashMap<>();
        #QUERY_PARAMS

        MultiValueMap<String, String> headerParams = new HashMap<>();
        #HEADER_PARAMS

        String body;
        #BODY

        MediaType sendType = #SEND_TYPE;

        MediaType receiveType = #RECEIVE_TYPE;

        ResponseEntity<String> responseEntity = Utils.forwardRequest(
                url, method, path, pathParams, queryParams, headerParams, body, sendType, receiveType
        );

        HttpStatus status = responseEntity.getStatusCode();
        headerParams = responseEntity.getHeaders();
        body = responseEntity.getBody();

        #RESPONSE

    } catch (Exception e) {
        throw new RuntimeException(e);
    }
}
