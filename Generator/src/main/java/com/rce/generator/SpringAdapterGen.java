package com.rce.generator;

import com.rce.common.io.ResultIO;
import com.rce.common.structures.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SpringAdapterGen {
    final static String BASE_PATH = "./src/main/resources/spring";

    static String CONTROLLER_TEMPLATE;
    static String PROCEDURE_TEMPLATE;
    static String RESPONSE_TEMPLATE;
    static Conversion CONVERSION;

    public static void main(String[] args) {
        try {
            CONTROLLER_TEMPLATE = Files.readString(Path.of(BASE_PATH + "/controllerTemplate.java"));
            PROCEDURE_TEMPLATE = Files.readString(Path.of(BASE_PATH + "/procedureTemplate.java"));
            RESPONSE_TEMPLATE = Files.readString(Path.of(BASE_PATH + "/responseTemplate.java"));

            CONVERSION = ResultIO.readFromYaml(BASE_PATH + "evolution.yml");

            String template = CONTROLLER_TEMPLATE;

            template = template.replace("#PROCEDURE", buildProcedures());

            BufferedWriter writer = new BufferedWriter(new FileWriter(BASE_PATH + "proxypy/adapterProxy.py"));
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
            Endpoint endpoint = Endpoint.fromString(method.endpointPrior);

            String template = PROCEDURE_TEMPLATE;

            template.replace("#OLD_PATH", endpointPrior.path);
            template.replace("#OLD_METHOD", endpointPrior.method.name().toLowerCase());
            template.replace("#PROCEDURE", "procedure"+(count++));
            template.replace("#SCHEME", "http"); //TODO
            template.replace("#HOST", "demo"); //TODO
            template.replace("#METHOD", endpoint.method.name().toUpperCase());
            template.replace("#PATH", endpoint.path);

            Message request = method.getRequest();

            StringBuilder pathParams = new StringBuilder();
            StringBuilder queryParams = new StringBuilder();
            StringBuilder headerParams = new StringBuilder();
            StringBuilder jsonParams = new StringBuilder();

            for (Parameter parameter: request.getParameters()) {
                if(parameter.key.startsWith("path")) {
                    pathParams.append("pathParams.put(")
                            .append(parameter.key.split("\\|")[1])
                            .append(parseResolution(parameter.resolution))
                            .append(");\n");
                }

                if(parameter.key.startsWith("query")) {
                    queryParams.append("queryParams.put(")
                            .append(parameter.key.split("\\|")[1])
                            .append(parseResolution(parameter.resolution))
                            .append(");\n");
                }

                if(parameter.key.startsWith("header")) {
                    headerParams.append("headerParams.put(")
                            .append(parameter.key.split("\\|")[1])
                            .append(parseResolution(parameter.resolution))
                            .append(");\n");
                }

                if(parameter.key.startsWith("json")) {
                    String prId = parameter.key.split("\\|")[1];
                    bodyParams.add(List.of(prId.split("\\.")));
                }
            }

            template.replace("#PATH_PARAMS", pathParams.toString());
            template.replace("#QUERY_PARAMS", queryParams.toString());
            template.replace("#HEADER_PARAMS", headerParams.toString());
            template.replace("#BODY", jsonParams.toString());

            template.replace("#SEND_TYPE", "APPLICATION_JSON"); //TODO
            template.replace("#RECEIVE_TYPE", "APPLICATION_JSON"); //TODO

            template.replace("#RESPONSE", buildResponse());

            procedures.append(template).append("\n");
        }
        return procedures.toString();
    }

    public static String parseResolution(String resolution) {
        String resolutionType = resolution.split("=")[0];
        String value = resolution.split("=")[1];

        if(resolutionType.equals("link")) {
            String linkType = value.split("\\|")[0];
            String priorParameterId = value.split("\\|")[1];

            switch (linkType) {
                case "path": {
                    return "_pathParams.get("+priorParameterId+");";
                }
                case "query": {
                    return "_queryParams.get("+priorParameterId+");";
                }
                case "header": {
                    return "_headerParams.get("+priorParameterId+");";
                }
                case "json": {
                    value = getValueFromBodyJson(priorParameterId);
                }
            }
        } else {
            return  "\"" + value + "\"";
        }
    }

    private static String getValueFromBodyJson(String parameterId) {
        StringBuilder valueBuilder = new StringBuilder();
        valueBuilder.append("body");
        for(String s : parameterId.split("\\.")) {
            valueBuilder
                    .append("[\"")
                    .append(s)
                    .append("\"]");
        }
        return valueBuilder.toString();
    }

    public static String buildResponse() {

    }
}
