package com.rce.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class Controller {

    private ObjectMapper mapper;

    public Controller() {
        mapper = new ObjectMapper();
    }

    @RequestMapping(
            value = "/user/service",
            method = RequestMethod.POST
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

            String path = "/user/{id}";

            Map<String, String> pathParams = new HashMap<>();
            pathParams.put("id", _body.get("id").textValue());

            Map<String,String> queryParams = new HashMap<>();


            Map<String, String> headerParams = new HashMap<>();


            String body = "{}";

            MediaType sendType = MediaType.valueOf("APPLICATION_JSON");

            MediaType receiveType = MediaType.valueOf("APPLICATION_JSON");

            ResponseEntity<String> responseEntity = Utils.forwardRequest(
                    scheme, host, method, path, pathParams, queryParams, headerParams, body, sendType, receiveType
            );

            HttpStatus status = responseEntity.getStatusCode();
            _headerParams = responseEntity.getHeaders().toSingleValueMap();
            _body = mapper.readTree(responseEntity.getBody());

            if(status.value() == 200) {
                HttpHeaders responseHeaders = new HttpHeaders();


                String responseBody = "{\"name\":\"" + "joão2" + "\",\"email\":\"" + _body.get("address").textValue() + "\",\"mainAddress\":\"" + _body.get("email").textValue() + "\"}"

                return ResponseEntity.status(200).headers(responseHeaders).body(responseBody);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(e.toString());
        }
        return ResponseEntity.internalServerError().body("UNMAPPED RESPONSE");
    }



    @RequestMapping(
            value = "/user/service",
            method = RequestMethod.PUT
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

            String path = "/user";

            Map<String, String> pathParams = new HashMap<>();


            Map<String,String> queryParams = new HashMap<>();


            Map<String, String> headerParams = new HashMap<>();


            String body = "{\"name\":\"" + "joão" + "\",\"email\":\"" + _headerParams.get("email") + "\",\"address\":\"" + _queryParams.get("mainAddress") + "\"}";

            MediaType sendType = MediaType.valueOf("APPLICATION_JSON");

            MediaType receiveType = MediaType.valueOf("APPLICATION_JSON");

            ResponseEntity<String> responseEntity = Utils.forwardRequest(
                    scheme, host, method, path, pathParams, queryParams, headerParams, body, sendType, receiveType
            );

            HttpStatus status = responseEntity.getStatusCode();
            headerParams = responseEntity.getHeaders();
            body = responseEntity.getBody();



        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



#
}
