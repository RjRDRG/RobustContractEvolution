package com.rce.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

import static com.rce.adapter.Utils.forwardRequest;
import static com.rce.adapter.Utils.forwardResponse;

@RestController
public class Controller {

    private ObjectMapper mapper;

    public Controller() {
        mapper = new ObjectMapper();
    }

    @RequestMapping(
            value = "/pets",
            method = RequestMethod.GET
    )
    public ResponseEntity<String> procedure0(@PathVariable Map<String, String> _pathParams,
                                             @RequestParam Map<String,String> _queryParams,
                                             @RequestHeader Map<String, String> _headerParams,
                                             @RequestBody String _rawBody) {
        try {
            JsonNode _body = mapper.readTree(_rawBody);

            String scheme = "http";

            String host = "demo";

            HttpMethod method = HttpMethod.valueOf("GET");

            String path = "/pets";

            Map<String, String> pathParams = new HashMap<>();


            Map<String,String> queryParams = new HashMap<>();
            queryParams.put("tags", _queryParams.get("tags"));queryParams.put("limit", _queryParams.get("limit"));

            Map<String, String> headerParams = new HashMap<>();


            String body = null;

            MediaType sendType = MediaType.valueOf("APPLICATION_JSON");

            MediaType receiveType = MediaType.valueOf("APPLICATION_JSON");

            ResponseEntity<String> responseEntity = forwardRequest(
                    scheme, host, method, path, pathParams, queryParams, headerParams, body, sendType, receiveType
            );

            HttpStatus status = responseEntity.getStatusCode();
            _headerParams = responseEntity.getHeaders().toSingleValueMap();
            _body = mapper.readTree(responseEntity.getBody());

            if(status.value() == 200) {
                HttpHeaders responseHeaders = new HttpHeaders();

                String responseBody = "{}";
                return forwardResponse(200, responseHeaders, responseBody);
            };
            HttpHeaders responseHeaders = new HttpHeaders();

            String responseBody = "{\"message\":\"" + _body.get("message").textValue() + "\",\"code\":\"" + _body.get("code").textValue() + "\"}";
            return forwardResponse(status.value(), responseHeaders, responseBody);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(e.toString());
        }
    }



    @RequestMapping(
            value = "/pets",
            method = RequestMethod.POST
    )
    public ResponseEntity<String> procedure1(@PathVariable Map<String, String> _pathParams,
                                             @RequestParam Map<String,String> _queryParams,
                                             @RequestHeader Map<String, String> _headerParams,
                                             @RequestBody String _rawBody) {
        try {
            JsonNode _body = mapper.readTree(_rawBody);

            String scheme = "http";

            String host = "demo";

            HttpMethod method = HttpMethod.valueOf("POST");

            String path = "/pets";

            Map<String, String> pathParams = new HashMap<>();


            Map<String,String> queryParams = new HashMap<>();


            Map<String, String> headerParams = new HashMap<>();
            headerParams.put("name", _headerParams.get("name"));

            String body = "{\"specie\":{\"class\":{\"genus\":\"" + "c0c0" + "\",\"family\":\"" + _body.get("specie").get("class").get("family").textValue() + "\",\"species\":\"" + _body.get("specie").get("class").get("species").textValue() + "\",\"order\":\"" + _body.get("specie").get("class").get("order").textValue() + "\",\"phylum\":\"" + _body.get("specie").get("class").get("phylum").textValue() + "\",\"kingdom\":\"" + _body.get("specie").get("class").get("kingdom").textValue() + "\"},\"name\":\"" + _body.get("specie").get("name").textValue() + "\",\"tag\":\"" + _body.get("specie").get("tag").textValue() + "\"},\"tag\":\"" + _body.get("tag").textValue() + "\"}";

            MediaType sendType = MediaType.valueOf("APPLICATION_JSON");

            MediaType receiveType = MediaType.valueOf("APPLICATION_JSON");

            ResponseEntity<String> responseEntity = forwardRequest(
                    scheme, host, method, path, pathParams, queryParams, headerParams, body, sendType, receiveType
            );

            HttpStatus status = responseEntity.getStatusCode();
            _headerParams = responseEntity.getHeaders().toSingleValueMap();
            _body = mapper.readTree(responseEntity.getBody());

            if(status.value() == 200) {
                HttpHeaders responseHeaders = new HttpHeaders();

                String responseBody = "{\"id\":\"" + _body.get("id").textValue() + "\"}";
                return forwardResponse(200, responseHeaders, responseBody);
            };
            HttpHeaders responseHeaders = new HttpHeaders();

            String responseBody = "{\"message\":\"" + _body.get("message").textValue() + "\",\"code\":\"" + _body.get("code").textValue() + "\"}";
            return forwardResponse(status.value(), responseHeaders, responseBody);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(e.toString());
        }
    }



    @RequestMapping(
            value = "/pet/{id}",
            method = RequestMethod.DELETE
    )
    public ResponseEntity<String> procedure2(@PathVariable Map<String, String> _pathParams,
                                             @RequestParam Map<String,String> _queryParams,
                                             @RequestHeader Map<String, String> _headerParams,
                                             @RequestBody String _rawBody) {
        try {
            JsonNode _body = mapper.readTree(_rawBody);

            String scheme = "http";

            String host = "demo";

            HttpMethod method = HttpMethod.valueOf("DELETE");

            String path = "/pets/{id}";

            Map<String, String> pathParams = new HashMap<>();
            pathParams.put("id", _pathParams.get("id"));

            Map<String,String> queryParams = new HashMap<>();


            Map<String, String> headerParams = new HashMap<>();


            String body = null;

            MediaType sendType = MediaType.valueOf("APPLICATION_JSON");

            MediaType receiveType = MediaType.valueOf("APPLICATION_JSON");

            ResponseEntity<String> responseEntity = forwardRequest(
                    scheme, host, method, path, pathParams, queryParams, headerParams, body, sendType, receiveType
            );

            HttpStatus status = responseEntity.getStatusCode();
            _headerParams = responseEntity.getHeaders().toSingleValueMap();
            _body = mapper.readTree(responseEntity.getBody());

            if(status.value() == 204) {
                HttpHeaders responseHeaders = new HttpHeaders();

                String responseBody = null;
                return forwardResponse(204, responseHeaders, responseBody);
            };
            HttpHeaders responseHeaders = new HttpHeaders();

            String responseBody = "{\"message\":\"" + _body.get("message").textValue() + "\",\"code\":\"" + _body.get("code").textValue() + "\"}";
            return forwardResponse(status.value(), responseHeaders, responseBody);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(e.toString());
        }
    }



    @RequestMapping(
            value = "/pet/{id}",
            method = RequestMethod.GET
    )
    public ResponseEntity<String> procedure3(@PathVariable Map<String, String> _pathParams,
                                             @RequestParam Map<String,String> _queryParams,
                                             @RequestHeader Map<String, String> _headerParams,
                                             @RequestBody String _rawBody) {
        try {
            JsonNode _body = mapper.readTree(_rawBody);

            String scheme = "http";

            String host = "demo";

            HttpMethod method = HttpMethod.valueOf("GET");

            String path = "/pets/{id}";

            Map<String, String> pathParams = new HashMap<>();
            pathParams.put("id", _pathParams.get("id"));

            Map<String,String> queryParams = new HashMap<>();


            Map<String, String> headerParams = new HashMap<>();


            String body = null;

            MediaType sendType = MediaType.valueOf("APPLICATION_JSON");

            MediaType receiveType = MediaType.valueOf("APPLICATION_JSON");

            ResponseEntity<String> responseEntity = forwardRequest(
                    scheme, host, method, path, pathParams, queryParams, headerParams, body, sendType, receiveType
            );

            HttpStatus status = responseEntity.getStatusCode();
            _headerParams = responseEntity.getHeaders().toSingleValueMap();
            _body = mapper.readTree(responseEntity.getBody());

            if(status.value() == 200) {
                HttpHeaders responseHeaders = new HttpHeaders();

                String responseBody = "{\"id\":\"" + _body.get("id").textValue() + "\"}";
                return forwardResponse(200, responseHeaders, responseBody);
            };
            HttpHeaders responseHeaders = new HttpHeaders();

            String responseBody = "{\"message\":\"" + _body.get("message").textValue() + "\",\"code\":\"" + _body.get("code").textValue() + "\"}";
            return forwardResponse(status.value(), responseHeaders, responseBody);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(e.toString());
        }
    }




}
