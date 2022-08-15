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
            value = #OLD_PATH,
            method = #OLD_METHOD
    )
    public ResponseEntity<String> #PROCEDURE(@PathVariable Map<String, String> pathParams,
                        @RequestParam Map<String,String> queryParams,
                        @RequestHeader Map<String, String> headerParams,
                        @RequestBody String body) {
        try {
            JsonNode node = mapper.readTree(body);
            node.textValue()

            String url = #URL;

            HttpMethod method = HttpMethod.valueOf(#METHOD);

            String path = #PATH;

            Map<String, String> pathParams = new HashMap<>();
            #PATH_PARAMS

            MultiValueMap<String,String> queryParams = new HashMap<>();
            #QUERY_PARAMS

            MultiValueMap<String, String> headerParams = new HashMap<>();
            #HEADER_PARAMS

            String body;
            #BODY

            MediaType sendType = MediaType.valueOf(#SEND_TYPE);

            MediaType receiveType = MediaType.valueOf(#RECEIVE_TYPE);

            ResponseEntity<String> responseEntity = sendRequest(
                url, method, path, pathParams, queryParams, headerParams, body, sendType, receiveType
            );

            HttpStatus status = responseEntity.getStatusCode();
            headerParams = responseEntity.getHeaders();
            body = responseEntity.getBody();

            #STATUS_CASES

            if(status == #STATUS) {
                HttpHeaders responseHeaders = new HttpHeaders();
                responseHeaders.set("Baeldung-Example-Header", "Value-ResponseEntityBuilderWithHttpHeaders");

                return ResponseEntity.ok()
                        .headers(responseHeaders)
                        .body("Response with header using ResponseEntity");
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    public ResponseEntity<String> sendRequest(
        String url,
        HttpMethod method,
        String path,
        Map<String, String> pathParams,
        MultiValueMap<String,String> queryParams,
        MultiValueMap<String, String> headerParams,
        String body,
        MediaType sendType,
        MediaType receiveType
    ) {
        String pathWithVariables = path;

        for (Map.Entry<String,String> entry : pathParams.entrySet()) {
            pathWithVariables = pathWithVariables.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        URI uri = UriComponentsBuilder.fromHttpUrl(url)
                .path(pathWithVariables)
                .queryParams(queryParams)
                .build().toUri();

        WebClient.RequestBodySpec requestBodySpec = WebClient.create()
                .method(method)
                .uri(uri);

        for (Map.Entry<String, List<String>> entry : headerParams.entrySet()) {
            requestBodySpec = requestBodySpec.header(entry.getKey(), entry.getValue().toArray(new String[0]));
        }

        return requestBodySpec
                .contentType(sendType)
                .accept(receiveType)
                .body(BodyInserters.fromValue(body))
                .retrieve()
                .onStatus(
                        status -> true,
                        clientResponse -> Mono.empty()
                )
                .toEntity(String.class)
                .block();
    }
}
