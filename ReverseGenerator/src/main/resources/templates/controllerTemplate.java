package com.rce.reverseadapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

import static com.rce.reverseadapter.Utils.forwardRequest;
import static com.rce.reverseadapter.Utils.forwardResponse;

@RestController
public class Controller {

    private final String HOST;
    private final int PORT;

    private final ObjectMapper mapper;

    public Controller() {
        mapper = new ObjectMapper();

        HOST = System.getenv("TARGET_HOST");
        PORT = Integer.parseInt(System.getenv("TARGET_PORT"));
    }

    #PROCEDURE#
}
