package com.rce.generator;

import com.rce.common.io.ResultIO;
import com.rce.common.structures.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ProxyPyAdapterGen {
    final static String BASE_PATH = "./src/main/resources/proxypy";
    final static String INDENT = "    ";


    public static void main1(String[] args) {
        try {
            ContractEvolution evolution = ResultIO.readFromYaml(BASE_PATH + "evolution.yml");
            String template = Files.readString(Path.of(BASE_PATH + "proxypy/proxy_template.txt"));

            template = template.replace("#ENDPOINT_MESSAGE_CASES", buildEndpointCases(evolution,4));
            template = template.replace("#ENDPOINT_MESSAGE_HANDLERS", buildEndpointHandlers(evolution,1));

            BufferedWriter writer = new BufferedWriter(new FileWriter(BASE_PATH + "proxypy/adapterProxy.py"));
            writer.write(template);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String buildEndpointCases(ContractEvolution evolution, int indentation) {
        StringBuilder casesBuilder = new StringBuilder();
        for (Method method : evolution.getMethods()) {
            Endpoint endpoint = Endpoint.fromString(method.endpointPrior);

            for (Message message : method.getMessages()) {
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

    private static String buildEndpointCase(Endpoint endpoint, Message message) {
        StringBuilder caseBuilder = new StringBuilder();
        caseBuilder.append("case");

        List<String> elms = endpoint.getPathElements();

        caseBuilder.append("[");
        for (String e : elms) {
            if (e.startsWith("{")) {
                caseBuilder
                        .append("priorPath_")
                        .append(e, 1, e.length() - 1);
            } else {
                caseBuilder.append("\"").append(e).append("\"");
            }
            caseBuilder.append(", ");
        }

        caseBuilder
                .append("\"")
                .append(endpoint.getMethod().name().toLowerCase());

        if(!message.typePrior.equals("request")) {
            caseBuilder
                    .append("\", \"")
                    .append(message.typePrior);
        }
        caseBuilder.append("\"]:");

        return caseBuilder.toString();
    }

    private static String buildEndpointHandlerName(Endpoint endpoint, Message message) {
        StringBuilder nameBuilder = new StringBuilder();

        nameBuilder
                .append("return self.handle_");

        List<String> vars = new ArrayList<>();

        for (String e : endpoint.getPathElements()) {
            if (e.startsWith("{")) {
                String var = e.substring(1, e.length()-1);
                vars.add("priorPath_" + var);
                nameBuilder.append(var);
            } else {
                nameBuilder.append(e);
            }
        }

        nameBuilder
                .append(endpoint.getMethod().name().toLowerCase())
                .append("_")
                .append(message.typePrior)
                .append("(");

        for(String var : vars) {
            nameBuilder.append(var).append(", ");
        }

        nameBuilder.append("message)");

        return nameBuilder.toString();
    }

    private static String buildEndpointHandlers(ContractEvolution evolution, int indentation) {
        StringBuilder handlersBuilder = new StringBuilder();

        for (Method method : evolution.getMethods()) {
            Endpoint endpoint = Endpoint.fromString(method.endpoint);
            Endpoint priorEndpoint = Endpoint.fromString(method.endpointPrior);

            for(Message message : method.getMessages()) {

                handlersBuilder
                        .append(buildEndpointHandlerDefinition(priorEndpoint, message, indentation))
                        .append("\n")
                        .append(buildEndpointHandlerImpl(endpoint, message, indentation + 1))
                        .append("\n\n");
            }
        }

        return handlersBuilder.toString();
    }

    private static String buildEndpointHandlerDefinition(Endpoint priorEndpoint, Message message, int indentation) {
        StringBuilder headerBuilder = new StringBuilder();

        headerBuilder
                .append(INDENT.repeat(indentation))
                .append("def handle_");

        List<String> vars = new ArrayList<>();

        for (String e : priorEndpoint.getPathElements()) {
            if (e.startsWith("{")) {
                String var = e.substring(1, e.length()-1);
                vars.add("priorPath_" + var);
                headerBuilder.append(var);
            } else {
                headerBuilder.append(e);
            }
        }

        headerBuilder
                .append(priorEndpoint.getMethod().name().toLowerCase())
                .append("_")
                .append(message.typePrior)
                .append("(\n")
                .append(INDENT.repeat(indentation))
                .append(INDENT)
                .append("self, ");

        for(String var : vars) {
            headerBuilder.append(var).append(", ");
        }

        headerBuilder
                .append("message: HttpParser,")
                .append("\n")
                .append(INDENT.repeat(indentation))
                .append(") -> Optional[HttpParser]:");

        return headerBuilder.toString();
    }

    private static String buildEndpointHandlerImpl(Endpoint endpoint, Message message, int indentation) {
        StringBuilder bodyBuilder = new StringBuilder();

        if(message.typePrior.equals("request")) {
            bodyBuilder.append(buildQueryParametersDictionary(indentation));
        }

        bodyBuilder.append(buildJsonBodyDictionary(indentation));

        List<String> queryParams = new ArrayList<>();
        List<String> headerParams = new ArrayList<>();
        List<List<String>> bodyParams = new ArrayList<>();

        for (Parameter parameter: message.getParameters()) {
            if(parameter.key.startsWith("query")) {
                String prId = parameter.key.split("\\|")[1];
                queryParams.add(prId);
            }

            if(parameter.key.startsWith("header")) {
                String prId = parameter.key.split("\\|")[1];
                headerParams.add(prId);
            }

            if(parameter.key.startsWith("json")) {
                String prId = parameter.key.split("\\|")[1];
                bodyParams.add(List.of(prId.split("\\.")));
            }

            bodyBuilder
                    .append(INDENT.repeat(indentation))
                    .append(buildEndpointRequestParameter(parameter))
                    .append("\n");
        }

        if(message.typePrior.equals("request")) {
            bodyBuilder.append(buildPath(endpoint, queryParams, indentation));
        }
        else {
            bodyBuilder.append(buildCode(message, indentation));
        }

        bodyBuilder
                .append(buildHeaders(headerParams, indentation));

        if(message.typePrior.equals("request")) {
            bodyBuilder
                    .append(buildRequestBody(bodyParams, indentation));
        }else {
            bodyBuilder
                    .append(buildResponseBody(bodyParams, indentation));
        }

        bodyBuilder
                .append(INDENT.repeat(indentation))
                .append("return message");

        return bodyBuilder.toString();
    }

    private static String buildQueryParametersDictionary(int indentation) {
        return  INDENT.repeat(indentation) +
                "query = self.build_query_dictionary(message)" +
                "\n";
    }

    private static String buildJsonBodyDictionary(int indentation) {
        return  INDENT.repeat(indentation) +
                "body = self.build_json_body_dictionary(message)" +
                "\n";
    }

    private static String buildEndpointRequestParameter(Parameter parameter) {
        StringBuilder parameterBuilder = new StringBuilder();

        String parameterType = parameter.getKey().split("\\|")[0];
        String parameterId = parameter.getKey().split("\\|")[1];

        switch (parameterType) {
            case "path": {
                parameterId = "path_" + parameterId;
                break;
            }
            case "query": {
                parameterId = "query_" + parameterId;
                break;
            }
            case "header": {
                parameterId = "header_" + parameterId;
                break;
            }
            case "json": {
                parameterId = "json_" + parameterId.replace(".","_");
                break;
            }
        }

        parameterBuilder
                .append(parameterId)
                .append(" = ");

        String resolutionType = parameter.getResolution().split("=")[0];
        String value = parameter.getResolution().split("=")[1];

        if(resolutionType.equals("link")) {
            String linkType = value.split("\\|")[0];
            String priorParameterId = value.split("\\|")[1];

            switch (linkType) {
                case "path": {
                    value = getValueFromPath(priorParameterId);
                    break;
                }
                case "query": {
                    value = getValueFromQuery(priorParameterId);
                    break;
                }
                case "header": {
                    value = getValueFromHeader(priorParameterId);
                    break;
                }
                case "json": {
                    value = getValueFromBodyJson(priorParameterId);
                    break;
                }
            }
        } else {
            value = "\"" + value + "\"";
        }

        parameterBuilder.append(value);

        return parameterBuilder.toString();
    }

    private static String getValueFromPath(String parameterId) {
        return "priorPath_" + parameterId;
    }

    private static String getValueFromQuery(String parameterId) {
        return "query[\"" + parameterId + "\"]";
    }

    private static String getValueFromHeader(String parameterId) {
        return "self.get_header(\"" + parameterId + "\", message)";
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

    private static String buildPath(Endpoint endpoint, List<String> queryParams, int indentation) {
        StringBuilder pathBuilder = new StringBuilder();

        pathBuilder
                .append(INDENT.repeat(indentation))
                .append("message.path = (\"/\" + ");

        List<String> elements = endpoint.getPathElements();
        for(int i=0; i<elements.size(); i++) {
            String s = elements.get(i);
            if(s.startsWith("{")) {
                pathBuilder.append("path_").append(s, 1, s.length()-1);
            }
            else {
                pathBuilder.append("\"").append(s).append("\"");
            }
            if(i!=elements.size()-1) {
                pathBuilder.append(" + \"/\" + ");
            }
        }

        if(!queryParams.isEmpty()) {
            pathBuilder.append(" + \"?");
            int count = 0;
            for (String p : queryParams) {
                pathBuilder
                        .append(p)
                        .append("=\" + ")
                        .append("query_")
                        .append(p);
                if(count != queryParams.size()-1) {
                    pathBuilder
                            .append(" + \"")
                            .append("&");
                }
                count++;
            }
        }

        pathBuilder.append(").encode()");

        pathBuilder
                .append("\n")
                .append(INDENT.repeat(indentation))
                .append("message.method = b\"")
                .append(endpoint.getMethod().name().toUpperCase())
                .append("\"")
                .append("\n");

        return pathBuilder.toString();
    }

    private static String buildCode(Message message, int indentation) {
        return INDENT.repeat(indentation) +
                "message.code = b'" +
                message.type +
                "'\n";
    }

    private static String buildHeaders(List<String> headerParams, int indentation) {
        StringBuilder headersBuilder = new StringBuilder();

        for(String p : headerParams) {
            headersBuilder
                    .append(INDENT.repeat(indentation))
                    .append("message.add_header(\"").append(p).append("\".encode(), ").append("header_").append(p).append(".encode())")
                    .append("\n");
        }

        return headersBuilder.toString();
    }

    private static String buildResponseBody(List<List<String>> bodyParams, int indentation) {
        if(bodyParams.isEmpty())
            return "";

        return INDENT.repeat(indentation) +
                "message.body = " +
                formatBodyJsonParameters(bodyParams) +
                "\n";
    }

    private static String buildRequestBody(List<List<String>> bodyParams, int indentation) {
        if(bodyParams.isEmpty())
            return "";

        return INDENT.repeat(indentation) +
                "message.update_body(" +
                "\n" +
                INDENT.repeat(indentation + 1) +
                formatBodyJsonParameters(bodyParams) +
                ",\n" +
                INDENT.repeat(indentation + 1) +
                "content_type=b'application/json'," +
                "\n" +
                INDENT.repeat(indentation) +
                ")" +
                "\n";
    }

    private static String formatBodyJsonParameters(List<List<String>> bodyParams) {
        class Node {
            final String id;
            final String valueId;
            final Set<Node> children;

            public Node(String id, String valueId, Set<Node> children) {
                this.id = id;
                this.valueId = valueId;
                this.children = children;
            }

            @Override
            public String toString() {
                if(children.isEmpty()) {
                    return "\"" + id + "\":\"' + json" +valueId+ " + '\"";
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

        for(List<String> p : bodyParams) {
            Node current = root;
            StringBuilder acum = new StringBuilder();
            for(String s : p) {
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

        contentBuilder.append("('{");

        int count=0;
        for (Node n : root.children) {
            if(count!=0)
                contentBuilder.append(", ");
            contentBuilder.append(n.toString());
            count++;
        }

        contentBuilder.append("}').encode()");

        return contentBuilder.toString();
    }

}