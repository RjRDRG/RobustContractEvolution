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
    static Conversion CONVERSION;

    public static void main(String[] args) {
        try {
            CONTROLLER_TEMPLATE = Files.readString(Path.of(BASE_PATH + "controllerTemplate.java"));
            PROCEDURE_TEMPLATE = Files.readString(Path.of(BASE_PATH + "procedureTemplate.java"));
            RESPONSE_TEMPLATE = Files.readString(Path.of(BASE_PATH + "responseTemplate.java"));

            CONVERSION = ResultIO.readFromYaml(BASE_PATH + "evolution.yml");

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
        for (Method method : CONVERSION.getMethods()) {
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
                            .append(parseResolution(parameter.resolution))
                            .append(");");
                }

                if(parameter.key.startsWith("query")) {
                    queryParams.append("queryParams.put(")
                            .append("\"")
                            .append(parameter.key.split("\\|")[1])
                            .append("\"")
                            .append(", ")
                            .append(parseResolution(parameter.resolution))
                            .append(");");
                }

                if(parameter.key.startsWith("header")) {
                    headerParams.append("headerParams.put(")
                            .append("\"")
                            .append(parameter.key.split("\\|")[1])
                            .append("\"")
                            .append(", ")
                            .append(parseResolution(parameter.resolution))
                            .append(");");
                }

                if(parameter.key.startsWith("json")) {
                    jsonParams.add(parameter);
                }
            }

            String body = formatBodyJsonParameters(jsonParams);
            for (Parameter parameter: jsonParams) {
                body = body.replace("#"+parameter.id(), "\" + " + parseResolution(parameter.resolution) + " + \"");
            }

            template = template.replace("#PATH_PARAMS#", pathParams.toString());
            template = template.replace("#QUERY_PARAMS#", queryParams.toString());
            template = template.replace("#HEADER_PARAMS#", headerParams.toString());

            template = template.replace("#BODY#", body);

            template = template.replace("#SEND_TYPE#", "\"APPLICATION_JSON\""); //TODO
            template = template.replace("#RECEIVE_TYPE#", "\"APPLICATION_JSON\""); //TODO

            template = template.replace("#RESPONSE#", buildResponse(method));

            procedures.append(template).append("\n\n\n");
        }
        return procedures.toString();
    }

    public static String buildResponse(Method method) {
        StringBuilder responses = new StringBuilder();
        for(Message response : method.getResponses()) {
            String template = RESPONSE_TEMPLATE;

            template = template.replace("#OLD_STATUS#", response.typePrior);

            StringBuilder headerParams = new StringBuilder();
            List<Parameter> jsonParams = new LinkedList<>();

            for (Parameter parameter: response.getParameters()) {
                if(parameter.key.startsWith("header")) {
                    headerParams.append("responseHeaders.set(")
                            .append("\"")
                            .append(parameter.key.split("\\|")[1])
                            .append("\"")
                            .append(", ")
                            .append(parseResolution(parameter.resolution))
                            .append(");");
                }

                if(parameter.key.startsWith("json")) {
                    jsonParams.add(parameter);
                }
            }

            String body = formatBodyJsonParameters(jsonParams);
            for (Parameter parameter: jsonParams) {
                body = body.replace("#"+parameter.id(), "\" + " + parseResolution(parameter.resolution) + " + \"");
            }

            template = template.replace("#RESPONSE_HEADERS#", headerParams.toString());
            template = template.replace("#RESPONSE_BODY#", body);

            template = template.replace("#STATUS#", response.type);

            responses.append(template).append("\n\n");
        }
        return responses.toString();
    }

    public static String parseResolution(String resolution) {
        String resolutionType = resolution.split("=")[0];
        String value = resolution.split("=")[1];

        if(resolutionType.equals("link")) {
            String linkType = value.split("\\|")[0];
            String priorParameterId = value.split("\\|")[1];

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
                    for(String s : priorParameterId.split("\\.")) {
                        valueBuilder
                                .append(".get(\"")
                                .append(s)
                                .append("\")");
                    }
                    return valueBuilder.append(".textValue()").toString();
                }
            }
        }

        return  "\"" + value + "\"";
    }

    private static String formatBodyJsonParameters(List<Parameter> bodyParams) {

        ObjectNode root = new ObjectMapper().createObjectNode();

        for(Parameter parameter : bodyParams) {
            ObjectNode current = root;
            String[] idSegments = parameter.idSegments();
            for(int i =0; i<idSegments.length; i++) {
                String s = idSegments[i];
                ObjectNode n = (ObjectNode) current.get(s);
                if(n==null) {
                    if(i == idSegments.length-1) {
                        current.put(s, "#"+parameter.id());
                    }
                    else {
                        n = current.objectNode();
                        current.set(s, n);
                        current = n;
                    }
                }
            }
        }

        String b = root.toString();
        b = b.replace("\"", "\\\"");

        return "\""+b+"\"";
    }
}
