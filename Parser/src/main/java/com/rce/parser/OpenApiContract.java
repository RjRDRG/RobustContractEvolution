package com.rce.parser;

import com.rce.parser.structures.Endpoint;
import com.rce.parser.structures.Property;
import com.rce.parser.structures.PropertyKey;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;

import java.util.*;

public class OpenApiContract implements IContract {

    OpenAPI api;

    public OpenApiContract(OpenAPI api) {
        this.api = api;
    }

    @Override
    public Set<Endpoint> getEndpoints() {
        Set<Endpoint> endpoints = new HashSet<>();

        for(Map.Entry<String,PathItem> item : api.getPaths().entrySet()) {
            endpoints.addAll(extractEndpointsKeys(item.getKey(), item.getValue()));
        }

        return endpoints;
    }

    @Override
    public List<String> getResponses(Endpoint endpoint) {
        Operation operation = extractOperation(endpoint);
        return new ArrayList<>(operation.getResponses().keySet());
    }

    @Override
    public Set<Property> getRequestProperties(Endpoint endpoint) {
        Operation operation = extractOperation(endpoint);

        Set<Property> propertySet = new HashSet<>();

        for(Parameter p : Optional.ofNullable(operation.getParameters()).orElse(Collections.emptyList())) {
            addPropertiesFromSchema(
                    propertySet,
                    new ExtendedSchema(
                            p.getSchema(),
                            PropertyKey.Location.valueOf(p.getIn().toUpperCase()),
                            Collections.emptyList(),
                            p.getName(),
                            p.getRequired()
                    )
            );
        }

        Optional<Map.Entry<String, MediaType>> entry = Optional.ofNullable(operation.getRequestBody())
                .flatMap(r -> r
                        .getContent()
                        .entrySet()
                        .stream()
                        .findFirst()
                );

        if(entry.isPresent()) {
            String mediaType = entry.get().getKey();
            Schema schema = entry.get().getValue().getSchema();

            if(mediaType.equals("application/json")) {
                addPropertiesFromSchema(
                        propertySet,
                        new ExtendedSchema(
                            schema,
                            PropertyKey.Location.JSON,
                            Collections.emptyList(),
                            null,
                            operation.getRequestBody().getRequired()
                        )
                );
            }
        }

        return propertySet;
    }

    @Override
    public Set<Property> getResponseProperties(Endpoint endpoint, String responseStatus) {
        Operation operation = extractOperation(endpoint);

        ApiResponse response = operation.getResponses().get(responseStatus);

        Set<Property> propertySet = new HashSet<>();

        for(Map.Entry<String, Header> entry : Optional.ofNullable(response.getHeaders()).orElse(Collections.emptyMap()).entrySet()) {
            addPropertiesFromSchema(
                    propertySet,
                    new ExtendedSchema(
                        entry.getValue().getSchema(),
                        PropertyKey.Location.HEADER,
                        Collections.emptyList(),
                        entry.getKey(),
                        Optional.ofNullable(entry.getValue().getRequired()).orElse(true)
                    )
            );
        }

        Optional<Map.Entry<String, MediaType>> entry = Optional.ofNullable(response.getContent())
                .flatMap(r -> r.entrySet().stream().findFirst());

        if(entry.isPresent()) {
            String mediaType = entry.get().getKey();
            Schema schema = entry.get().getValue().getSchema();

            if(mediaType.equals("application/json")) {
                addPropertiesFromSchema(
                        propertySet,
                        new ExtendedSchema(
                            schema,
                            PropertyKey.Location.JSON,
                            Collections.emptyList(),
                            null,
                            true
                        )
                );
            }
        }

        return propertySet;
    }

    //------------------------------------------------------------------------------------------------------------------

    private void addPropertiesFromSchema(Set<Property> propertySet, ExtendedSchema es) {
        if(es.schema.getType().equals("object")) {
            Map<String, Schema> schemaProperties = es.schema.getProperties();
            for (Map.Entry<String, Schema> entry : schemaProperties.entrySet()) {
                List<String> newPrecursors = new LinkedList<>(es.precursors);
                if(es.name != null)
                    newPrecursors.add(es.name);
                addPropertiesFromSchema(
                        propertySet,
                        new ExtendedSchema(
                                entry.getValue(),
                                es.location,
                                newPrecursors,
                                entry.getKey(),
                                es.schema.getRequired().contains(entry.getKey())
                        )
                );
            }
        }
        else if(es.schema.getType().matches("integer|string|number|boolean")) {
            propertySet.add(
                    new Property(
                            es.location,
                            es.precursors,
                            es.name,
                            false,
                            es.schema.getType(),
                            es.schema.getFormat(),
                            es.required,
                            !es.required ? String.valueOf(es.schema.getDefault()) : null
                    )
            );
        }
        else if(es.schema.getType().matches("array")) {
            propertySet.add(
                    new Property(
                            es.location,
                            es.precursors,
                            es.name,
                            true,
                            null, //TODO. swagger parser doesn't support items schema in arrays yet v2.0.31.
                            null,
                            es.required,
                            !es.required ? String.valueOf(es.schema.getDefault()) : null
                    )
            );
        }
        // TODO. dictionary type. key is present in schema.type, value type is present in schema.additionalProperties.?
    }

    private Set<Endpoint> extractEndpointsKeys(String path, PathItem item) {
        Set<Endpoint> keys = new HashSet<>();
        if(item.getGet()!=null) {
            keys.add(new Endpoint(path, Endpoint.Method.GET));
        }
        if(item.getPost()!=null) {
            keys.add(new Endpoint(path, Endpoint.Method.POST));
        }
        if(item.getPut()!=null) {
            keys.add(new Endpoint(path, Endpoint.Method.PUT));
        }
        if(item.getDelete()!=null) {
            keys.add(new Endpoint(path, Endpoint.Method.DELETE));
        }
        if(item.getHead()!=null) {
            keys.add(new Endpoint(path, Endpoint.Method.HEAD));
        }
        if(item.getOptions()!=null) {
            keys.add(new Endpoint(path, Endpoint.Method.OPTIONS));
        }
        if(item.getPatch()!=null) {
            keys.add(new Endpoint(path, Endpoint.Method.PATCH));
        }
        if(item.getTrace()!=null) {
            keys.add(new Endpoint(path, Endpoint.Method.TRACE));
        }
        return keys;
    }

    private Operation extractOperation(Endpoint endpoint) {
        PathItem path = api.getPaths().get(endpoint.path);
        Operation operation;
        switch (endpoint.method) {
            case GET:
                operation = path.getGet();
                break;
            case PUT:
                operation = path.getPut();
                break;
            case HEAD:
                operation = path.getHead();
                break;
            case POST:
                operation = path.getPost();
                break;
            case PATCH:
                operation = path.getPatch();
                break;
            case TRACE:
                operation = path.getTrace();
                break;
            case DELETE:
                operation = path.getDelete();
                break;
            case OPTIONS:
                operation = path.getOptions();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + endpoint.method);
        }
        return operation;
    }

    static class ExtendedSchema {
        public final Schema schema;
        public final PropertyKey.Location location;
        public final List<String> precursors;
        public final String name;
        public final boolean required;

        public ExtendedSchema(Schema schema, PropertyKey.Location location, List<String> precursors, String name, boolean required) {
            this.schema = schema;
            this.location = location;
            this.precursors = precursors;
            this.name = name;
            this.required = required;
        }
    }
}
