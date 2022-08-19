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

    #PROCEDURE#
}
