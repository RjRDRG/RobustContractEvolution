package com.rce.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rce.common.io.ResultIO;
import com.rce.common.structures.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class SpringAdapterGen {
    final static String BASE_PATH = "./src/main/resources/spring/";

    static String CONTROLLER_TEMPLATE;
    static String PROCEDURE_TEMPLATE;
    static String RESPONSE_TEMPLATE;
    static ContractEvolution Evolution;

    public static void main(String[] args) {
        try {
            CONTROLLER_TEMPLATE = Files.readString(Path.of(BASE_PATH + "controllerTemplate.java"));
            PROCEDURE_TEMPLATE = Files.readString(Path.of(BASE_PATH + "procedureTemplate.java"));
            RESPONSE_TEMPLATE = Files.readString(Path.of(BASE_PATH + "responseTemplate.java"));

            Evolution = ResultIO.readFromYaml(BASE_PATH + "evolution1.yml");

            String template = CONTROLLER_TEMPLATE;

            template = template.replace("#PROCEDURE#", buildProcedures());

            BufferedWriter writer = new BufferedWriter(new FileWriter(BASE_PATH + "/Controller.java"));
            writer.write(template);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static String buildProcedures() {
        StringBuilder procedures = new StringBuilder();
        int count = 0;
        for (Method method : Evolution.getMethods()) {
            Endpoint endpointPrior = Endpoint.fromString(method.endpointPrior);
            Endpoint endpoint = Endpoint.fromString(method.endpoint);

            String template = PROCEDURE_TEMPLATE;

            template = template.replace("#OLD_PATH#", "\"" + endpointPrior.path + "\"");
            template = template.replace("#OLD_METHOD#", "RequestMethod." + endpointPrior.method.name().toUpperCase());
            template = template.replace("#PROCEDURE#", "procedure"+(count++));
            template = template.replace("#SCHEME#", "\"http\""); //TODO
            template = template.replace("#HOST#", "\"demo\""); //TODO
            template = template.replace("#METHOD#", "\"" + endpoint.method.name().toUpperCase() + "\"");
            template = template.replace("#PATH#", "\"" + endpoint.path + "\"");

            Message request = method.getRequest();

            StringBuilder pathParams = new StringBuilder();
            StringBuilder queryParams = new StringBuilder();
            StringBuilder headerParams = new StringBuilder();
            List<Parameter> jsonParams = new LinkedList<>();

            for (Parameter parameter: request.getParameters()) {
                if(parameter.key.startsWith("path")) {
                    pathParams.append("pathParams.put(")
                            .append("\"")
                            .append(parameter.key.split("\\|")[1])
                            .append("\"")
                            .append(", ")
                            .append(parseResolution(parameter))
                            .append(");");
                }

                if(parameter.key.startsWith("query")) {
                    queryParams.append("queryParams.put(")
                            .append("\"")
                            .append(parameter.key.split("\\|")[1])
                            .append("\"")
                            .append(", ")
                            .append(parseResolution(parameter))
                            .append(");");
                }

                if(parameter.key.startsWith("header")) {
                    headerParams.append("headerParams.put(")
                            .append("\"")
                            .append(parameter.key.split("\\|")[1])
                            .append("\"")
                            .append(", ")
                            .append(parseResolution(parameter))
                            .append(");");
                }

                if(parameter.key.startsWith("json")) {
                    jsonParams.add(parameter);
                }
            }

            String body = formatBodyJsonParameters(jsonParams);

            template = template.replace("#PATH_PARAMS#", pathParams.toString());
            template = template.replace("#QUERY_PARAMS#", queryParams.toString());
            template = template.replace("#HEADER_PARAMS#", headerParams.toString());

            template = template.replace("#BODY#", String.valueOf(body));

            template = template.replace("#SEND_TYPE#", "\"APPLICATION_JSON\""); //TODO
            template = template.replace("#RECEIVE_TYPE#", "\"APPLICATION_JSON\""); //TODO

            template = template.replace("#RESPONSE#", buildResponse(method));

            procedures.append(template).append("\n\n\n");
        }
        return procedures.toString();
    }



    public static String buildResponse(Method method) {
        StringBuilder responses = new StringBuilder();
        String defaultCase = "return ResponseEntity.internalServerError().body(\"UNMAPPED RESPONSE\");";
        for(Message response : method.getResponses()) {
            String template;
            if(response.typePrior.equalsIgnoreCase("default")) {
                template = RESPONSE_TEMPLATE;
                template = template.replace("#STATUS#", "status.value()");
            } else {
                template = "if(status.value() == #OLD_STATUS#) {\n\t#CASE#\n};";
                template= template.replace("#CASE#", RESPONSE_TEMPLATE);
                template = template.replace("#OLD_STATUS#", response.typePrior);
                template = template.replace("#STATUS#", response.type);
            }

            StringBuilder headerParams = new StringBuilder();
            List<Parameter> jsonParams = new LinkedList<>();

            for (Parameter parameter: response.getParameters()) {
                if(parameter.key.startsWith("header")) {
                    headerParams.append("responseHeaders.set(")
                            .append("\"")
                            .append(parameter.key.split("\\|")[1])
                            .append("\"")
                            .append(", ")
                            .append(parseResolution(parameter))
                            .append(");");
                }

                if(parameter.key.startsWith("json")) {
                    jsonParams.add(parameter);
                }
            }

            String body = formatBodyJsonParameters(jsonParams);

            template = template.replace("#RESPONSE_HEADERS#", headerParams.toString());
            template = template.replace("#RESPONSE_BODY#", String.valueOf(body));

            if(response.typePrior.equalsIgnoreCase("default"))
                defaultCase = template;
            else
                responses.append(template).append("\n");
        }
        responses.append(defaultCase).append("\n");
        return responses.toString();
    }

    public static String parseResolution(Parameter parameter) {
        String resolutionType = parameter.resolutionType();

        if(resolutionType.equalsIgnoreCase("link")) {
            String linkType = parameter.resolutionLinkType();
            String priorParameterId = parameter.resolutionLinkId();

            switch (linkType) {
                case "path": {
                    return "_pathParams.get(\""+priorParameterId+"\")";
                }
                case "query": {
                    return "_queryParams.get(\""+priorParameterId+"\")";
                }
                case "header": {
                    return "_headerParams.get(\""+priorParameterId+"\")";
                }
                case "json": {
                    StringBuilder valueBuilder = new StringBuilder();
                    valueBuilder.append("_body");
                    if(priorParameterId!=null)
                        for(String s : priorParameterId.split("\\.")) {
                            valueBuilder
                                    .append(".get(\"")
                                    .append(s)
                                    .append("\")");
                        }
                    if(parameter.type.equals(Parameter.Type.Object))
                        return valueBuilder.append(".toString()").toString();
                    else
                        return  valueBuilder.append(".textValue()").toString();
                }
            }
        }

        switch (parameter.type) {
            case String:
                return  "\"" + parameter.resolutionValue() + "\"";
            case Number:
            case Object:
            case Array:
            case Boolean:
            default:
                return parameter.resolutionValue();
        }
    }

    private static String formatBodyJsonParameters(List<Parameter> parameters) {

        if(parameters.isEmpty())
            return null;

        if(parameters.size() == 1 && parameters.get(0).id() == null) { //Handle json primitive types
            Parameter parameter = parameters.get(0);
            switch (parameter.type) {
                case String:
                    return "\"\\\"\" +" + parseResolution(parameter) + "+ \"\\\"\"";
                case Number:
                case Boolean:
                case Array:
                case Object:
                    return parseResolution(parameter);
            }
        }

        ObjectNode root = new ObjectMapper().createObjectNode();

        for(Parameter parameter : parameters) {
            ObjectNode current = root;
            String[] idSegments = parameter.idSegments();
            for(int i=0; i<idSegments.length; i++) {
                String s = idSegments[i];
                ObjectNode n = (ObjectNode) current.get(s);
                if(n==null) {
                    if(i == idSegments.length-1) {
                        current.put(s, "#"+parameter.id());
                    }
                    else {
                        n = current.objectNode();
                        current.set(s, n);
                    }
                }
                current = n;
            }
        }

        String body = root.toString();
        body = body.replace("\"", "\\\"");

        body = "\""+body+"\"";

        for (Parameter parameter: parameters) {
            switch (parameter.type) {
                case String:
                    body = body.replace("#"+parameter.id(), "\" + " + parseResolution(parameter) + " + \"");
                    break;
                case Number:
                case Boolean:
                case Array:
                case Object:
                    body = body.replace("\\\"#"+parameter.id()+"\\\"", "\" + " + parseResolution(parameter) + " + \"");
                    break;
            }
        }

        return body;
    }
}
