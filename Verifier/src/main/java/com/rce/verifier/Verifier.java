package com.rce.verifier;

import com.rce.common.io.ResultIO;
import com.rce.common.structures.*;
import com.rce.parser.IContract;
import com.rce.parser.OpenApiContract;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.parser.core.models.ParseOptions;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

public class Verifier {

    public static String BasePath = "./src/main/resources/";

    public static void main(String[] args) throws FileNotFoundException {

        Conversion conversion = ResultIO.readFromYaml(BasePath + "evolution.yml");

        ParseOptions parseOptions = new ParseOptions();
        parseOptions.setResolve(true);
        parseOptions.setResolveFully(true);

        IContract newContract = new OpenApiContract(new OpenAPIParser().readLocation(BasePath + "new.yml", null, parseOptions).getOpenAPI());
        IContract oldContract = new OpenApiContract(new OpenAPIParser().readLocation(BasePath + "old.yml", null, parseOptions).getOpenAPI());

        for(Endpoint endpoint : newContract.getEndpoints()) {
            List<Method> method = conversion.getMethods().stream().filter(m->m.endpoint.equals(endpoint.toString())).collect(Collectors.toList());
            assert method.size() == 1;

            List<Message> requestMessage = method.get(0).getMessages().stream().filter(m->m.type.equals("Request")).collect(Collectors.toList());
            assert requestMessage.size() == 1;
            for (Property property : newContract.getRequestProperties(endpoint)) {
                List<Parameter> parameter = requestMessage.get(0).parameters.stream().filter(p-> p.key.equals(property.key.toString())).collect(Collectors.toList());
                assert parameter.size() == 1;
                assert verifyResolution(parameter.get(0).resolution, property, oldContract, method.get(0).endpointPrior, requestMessage.get(0).typePrior);
            }

            for(String response : newContract.getResponses(endpoint)){
                List<Message> responseMessage = method.get(0).getMessages().stream().filter(m->m.type.equals(response)).collect(Collectors.toList());
                assert responseMessage.size() == 1;
                for (Property property : newContract.getResponseProperties(endpoint,response)) {
                    List<Parameter> parameter = responseMessage.get(0).parameters.stream().filter(p-> p.key.equals(property.key.toString())).collect(Collectors.toList());
                    assert parameter.size() == 1;
                    assert verifyResolution(parameter.get(0).resolution, property, oldContract, method.get(0).endpointPrior, responseMessage.get(0).typePrior);
                }
            }
        }
    }

    private static boolean verifyResolution(String resolution, Property property, IContract oldContract, String priorEndpoint, String priorMessage) {
        String[] s0 = resolution.split(Resolution.TypeSeparator);
        Resolution.Type resolutionType = Resolution.Type.valueOf(s0[0].toUpperCase());
        switch (resolutionType) {
            case VALUE: {
                return verifyValue(s0[1], property);
            }
            case LINK: {
                return verifyLink(s0[1], property);
            }
            default: return false;
        }
    }

    private static boolean verifyValue(String value, Property property) {
        // TODO
        return true;
    }

    private static boolean verifyLink(String link, Property property) {
        //TODO
        return true;
    }
}