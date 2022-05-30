package com.rce.generator;

import com.rce.common.io.ResultIO;
import com.rce.common.structures.*;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Generator {
    public static String BasePath = "./src/main/resources/";

    public static void main(String[] args) {
        try {
            Result result = ResultIO.readFromYaml(BasePath + "evolution.yml");
            String template = Files.readString(Path.of(BasePath + "proxy_template.txt"));

            template = template.replace("#ENDPOINT_REQUEST_CASES", buildEndpointRequestCases(result,4));
            template = template.replace("#ENDPOINT_REQUEST_HANDLERS", buildEndpointRequestHandlers(result,1));

            BufferedWriter writer = new BufferedWriter(new FileWriter(BasePath + "proxy.py"));
            writer.write(template);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String buildEndpointRequestCases(Result result, int indentation) {
        StringBuilder casesBuilder = new StringBuilder();
        for (Method method : result.getMethods()) {
            Endpoint endpoint = Endpoint.fromString(method.endpointPrior);

            casesBuilder
                    .append("\t".repeat(indentation))
                    .append(buildEndpointRequestCase(endpoint))
                    .append("\n").append("\t".repeat(indentation)).append("\t")
                    .append(buildEndpointRequestMethodName(endpoint))
                    .append("\n");
        }

        casesBuilder
                .append("\t".repeat(indentation))
                .append("case _:")
                .append("\n").append("\t".repeat(indentation)).append("\t")
                .append("return request");

        return casesBuilder.toString();
    }

    private static String buildEndpointRequestCase(Endpoint endpoint) {
        StringBuilder caseBuilder = new StringBuilder();
        caseBuilder.append("case ");

        List<String> elms = endpoint.getPathElements();

        for (int i = 0; i < elms.size(); i++) {
            String e = elms.get(i);
            caseBuilder.append("[");
            if (e.startsWith("{")) {
                caseBuilder
                        .append("priorPath_")
                        .append(e, 1, e.length()-1);
            } else {
                caseBuilder.append("\"").append(e).append("\"");
            }
            caseBuilder.append("], ");
        }

        caseBuilder.append("[\"").append(endpoint.getMethod().name().toLowerCase()).append("\"]:");

        return caseBuilder.toString();
    }

    private static String buildEndpointRequestMethodName(Endpoint endpoint) {
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
                .append("_request(");

        for(String var : vars) {
            nameBuilder.append(var).append(", ");
        }

        nameBuilder.append("request)");

        return nameBuilder.toString();
    }

    private static String buildEndpointRequestHandlers(Result result, int indentation) {
        StringBuilder handlersBuilder = new StringBuilder();

        for (Method method : result.getMethods()) {
            handlersBuilder
                    .append(buildEndpointRequestHandler(method, indentation))
                    .append("\n\n");
        }

        return handlersBuilder.toString();
    }

    private static String buildEndpointRequestHandler(Method method, int indentation) {
        StringBuilder handlerBuilder = new StringBuilder();

        Endpoint endpoint = Endpoint.fromString(method.endpoint);
        Endpoint priorEndpoint = Endpoint.fromString(method.endpointPrior);
        Message requestMessage = method.getMessages().stream().filter(m->m.type.equals(Message.REQUEST)).findFirst().get();

        handlerBuilder
                .append(buildEndpointRequestHandlerDefinition(priorEndpoint,indentation))
                .append("\n")
                .append(buildEndpointRequestHandlerImpl(endpoint, requestMessage, indentation+1));

        return handlerBuilder.toString();
    }

    private static String buildEndpointRequestHandlerDefinition(Endpoint priorEndpoint, int indentation) {
        StringBuilder headerBuilder = new StringBuilder();

        headerBuilder
                .append("\t".repeat(indentation))
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
                .append("_request(\n")
                .append("\t".repeat(indentation))
                .append("\t")
                .append("self, ");

        for(String var : vars) {
            headerBuilder.append(var).append(", ");
        }

        headerBuilder
                .append("request: HttpParser,")
                .append("\n")
                .append("\t".repeat(indentation))
                .append(") -> Optional[HttpParser]:");

        return headerBuilder.toString();
    }

    private static String buildEndpointRequestHandlerImpl(Endpoint endpoint, Message requestMessage, int indentation) {
        StringBuilder bodyBuilder = new StringBuilder();

        bodyBuilder
                .append(buildQueryParametersDictionary(indentation))
                .append(buildJsonBodyDictionary(indentation))
                .append("\n");

        List<String> queryParams = new ArrayList<>();
        List<String> headerParams = new ArrayList<>();

        for (Parameter parameter: requestMessage.getParameters()) {
            if(parameter.key.startsWith("query")) {
                String prId = parameter.key.split("\\|")[1];
                queryParams.add(prId);
            }

            if(parameter.key.startsWith("header")) {
                String prId = parameter.key.split("\\|")[1];
                headerParams.add(prId);
            }

            bodyBuilder
                    .append("\t".repeat(indentation))
                    .append(buildEndpointRequestParameter(parameter))
                    .append("\n");
        }

        bodyBuilder
                .append("\n")
                .append("\t".repeat(indentation))
                .append(buildPath(endpoint, queryParams))
                .append(buildHeaders(headerParams, indentation))
                .append(buildBody(Collections.emptyList(), indentation))
                .append("\t".repeat(indentation))
                .append("return request");

        return bodyBuilder.toString();
    }

    private static String buildQueryParametersDictionary(int indentation) {
        return  "\t".repeat(indentation) +
                "query = self.build_query_dictionary(request)" +
                "\n";
    }

    private static String buildJsonBodyDictionary(int indentation) {
        return  "\t".repeat(indentation) +
                "jsonBody = self.build_json_body_dictionary(request)" +
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
        return "self.get_header(\"" + parameterId + "\", request)";
    }

    private static String getValueFromBodyJson(String parameterId) {
        StringBuilder valueBuilder = new StringBuilder();
        valueBuilder.append("jsonBody");
        for(String s : parameterId.split("\\.")) {
            valueBuilder
                    .append("[\"")
                    .append(s)
                    .append("\"]");
        }
        return valueBuilder.toString();
    }

    private static String buildPath(Endpoint endpoint, List<String> queryParams) {
        StringBuilder pathBuilder = new StringBuilder();

        pathBuilder.append("request.path = \"/\" + ");

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

        pathBuilder.append("\n");

        return pathBuilder.toString();
    }

    private static String buildHeaders(List<String> headerParams, int indentation) {
        StringBuilder headersBuilder = new StringBuilder();

        for(String p : headerParams) {
            headersBuilder
                    .append("\t".repeat(indentation))
                    .append("request.add_header(\"").append(p).append("\".encode(), ").append("header_").append(p).append(".encode())")
                    .append("\n");
        }

        return headersBuilder.toString();
    }

    private static String buildBody(List<List<String>> bodyParams, int indentation) {
        if(bodyParams.isEmpty())
            return "";

        StringBuilder bodyBuilder = new StringBuilder();

        bodyBuilder
                .append("\t".repeat(indentation))
                .append("request.update_body(")
                .append("\n")
                .append("\t".repeat(indentation+1))
                .append("TODO,")
                .append("\n")
                .append("\t".repeat(indentation+1))
                .append("content_type=b'application/json',")
                .append("\n")
                .append("\t".repeat(indentation))
                .append(")")
                .append("\n")
                .append("\n");

        return bodyBuilder.toString();
    }

    private String formatBodyJsonParameters(List<List<String>> bodyParams) {
        class Node {
            String id;
            String valueId;
            Set<Node> children;

            public Node(String id, Set<Node> children) {
                this.id = id;
                this.children = children;
            }

            @Override
            public String toString() {
                if(children.isEmpty()) {
                    return "\"" + id + "\": json_"+valueId;
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

        Node root = new Node("", new HashSet<>());

        for(List<String> p : bodyParams) {
            Node current = root;
            for(String s : p) {
                Node newNode = new Node(s, new HashSet<>());
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

        contentBuilder.append("b'{");

        int count=0;
        for (Node n : root.children) {
            contentBuilder.append(n.toString());
            if(count!=root.children.size()-1)
                contentBuilder.append(", ");
            count++;
        }

        contentBuilder.append("}',");

        return contentBuilder.toString();
    }

    /*
            if request.body:
            request.update_body(
                b'{"key": "modified"}',
                content_type=b'application/json',
            )
            */
}