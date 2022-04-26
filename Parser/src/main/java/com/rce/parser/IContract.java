package com.rce.parser;

import com.rce.parser.structures.Endpoint;
import com.rce.parser.structures.Property;

import java.util.List;
import java.util.Set;

public interface IContract {

    Set<Endpoint> getEndpoints();

    List<String> getResponses(Endpoint endpoint);

    Set<Property> getRequestProperties(Endpoint endpoint);

    Set<Property> getResponseProperties(Endpoint endpoint, String status);
}
