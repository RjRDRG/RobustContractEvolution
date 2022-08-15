package com.rce.generator;

import com.rce.common.io.ResultIO;
import com.rce.common.structures.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

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
            List<Parameter> jsonParams = new LinkedList<>();

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
                    jsonParams.add(parameter);
                }
            }

            template.replace("#PATH_PARAMS", pathParams.toString());
            template.replace("#QUERY_PARAMS", queryParams.toString());
            template.replace("#HEADER_PARAMS", headerParams.toString());
            template.replace("#BODY", formatBodyJsonParameters(jsonParams));

            template.replace("#SEND_TYPE", "APPLICATION_JSON"); //TODO
            template.replace("#RECEIVE_TYPE", "APPLICATION_JSON"); //TODO

            template.replace("#RESPONSE", buildResponse());

            procedures.append(template).append("\n");
        }
        return procedures.toString();
    }

    public static String buildResponse() {

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
                    StringBuilder valueBuilder = new StringBuilder();
                    valueBuilder.append("_body");
                    for(String s : priorParameterId.split("\\.")) {
                        valueBuilder
                                .append(".get(")
                                .append(s)
                                .append(")");
                    }
                    return valueBuilder.append(".textValue();").toString();
                }
            }
        } else {
            return  "\"" + value + "\"";
        }
    }

    private static String formatBodyJsonParameters(List<Parameter> bodyParams) {
        class Node {
            final String id;
            final String value;
            final Set<Node> children;

            public Node(String id, String value, Set<Node> children) {
                this.id = id;
                this.value = value;
                this.children = children;
            }

            @Override
            public String toString() {
                if(children.isEmpty()) {
                    return "\"" + id + "\": + " + value + " + '\""; //TODO
                }
                else {
                    StringBuilder objectBuilder = new StringBuilder();
                    objectBuilder
                            .append("\"")
                            .append(id)
                            .append("\":{");

                    int count = 0;
                    for (Node n: children) {
                        if(count!=0) {
                            objectBuilder.append(", ");
                        }
                        objectBuilder.append(n.toString());
                        count++;
                    }

                    objectBuilder.append("}");

                    return objectBuilder.toString();
                }
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Node node = (Node) o;
                return id.equals(node.id);
            }

            @Override
            public int hashCode() {
                return Objects.hash(id);
            }
        }

        Node root = new Node("", "", new HashSet<>());

        for(Parameter p : bodyParams) {
            Node current = root;
            StringBuilder acum = new StringBuilder();
            for(String s : List.of(p.key.split("\\|")[1].split("\\."))) {
                acum.append("_").append(s);
                Node newNode = new Node(s, acum.toString(), new HashSet<>());
                if(!current.children.contains(newNode)) {
                    current.children.add(newNode);
                    current = newNode;
                }
                else {
                    current = current.children.stream().filter(c->c.id.equals(s)).findAny().get();
                }
            }
        }

        StringBuilder contentBuilder = new StringBuilder();

        contentBuilder.append("{");

        int count=0;
        for (Node n : root.children) {
            if(count!=0)
                contentBuilder.append(", ");
            contentBuilder.append(n.toString());
            count++;
        }

        contentBuilder.append("}");

        return contentBuilder.toString();
    }
}
