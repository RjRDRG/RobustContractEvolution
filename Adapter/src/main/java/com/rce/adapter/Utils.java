package com.rce.adapter;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.Map;

public class Utils {

    public static ResponseEntity<String> forwardRequest(
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
