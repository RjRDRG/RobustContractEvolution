package com.rce.generator;

import com.rce.common.io.ResultIO;
import com.rce.common.structures.Conversion;
import com.rce.common.structures.Endpoint;
import com.rce.common.structures.Message;
import com.rce.common.structures.Method;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SpringAdapterGen {
    final static String BASE_PATH = "./src/main/resources/spring";

    static String CONTROLLER_TEMPLATE;
    static String PROCEDURE_TEMPLATE;
    static String RESPONSE_TEMPLATE;
    static Conversion conversion;

    public static void main(String[] args) {
        try {
            CONTROLLER_TEMPLATE = Files.readString(Path.of(BASE_PATH + "/controllerTemplate.java"));
            PROCEDURE_TEMPLATE = Files.readString(Path.of(BASE_PATH + "/procedureTemplate.java"));
            RESPONSE_TEMPLATE = Files.readString(Path.of(BASE_PATH + "/responseTemplate.java"));

            Conversion conversion = ResultIO.readFromYaml(BASE_PATH + "evolution.yml");

            String template = CONTROLLER_TEMPLATE;

            template = template.replace("#PROCEDURE", buildProcedures());
            template = template.replace("#ENDPOINT_MESSAGE_HANDLERS", buildEndpointHandlers(conversion,1));

            BufferedWriter writer = new BufferedWriter(new FileWriter(BASE_PATH + "proxypy/adapterProxy.py"));
            writer.write(template);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String buildProcedures() {
        StringBuilder casesBuilder = new StringBuilder();
        for (Method method : conversion.getMethods()) {
            Endpoint endpoint = Endpoint.fromString(method.endpointPrior);

            String template = PROCEDURE_TEMPLATE;

            Message request = method.getRequest();

            template.replace(); //TODO

            for (Message message : method.getResponses()) {
                casesBuilder
                        .append(INDENT.repeat(indentation))
                        .append(buildEndpointCase(endpoint, message))
                        .append("\n").append(INDENT.repeat(indentation)).append(INDENT)
                        .append(buildEndpointHandlerName(endpoint, message))
                        .append("\n");
            }
        }

        casesBuilder
                .append(INDENT.repeat(indentation))
                .append("case _:")
                .append("\n").append(INDENT.repeat(indentation)).append(INDENT)
                .append("return message");

        return casesBuilder.toString();
    }

    public static String buildResponse() {

    }
}
