import com.rce.common.structures.Endpoint;
import com.rce.common.structures.Property;
import com.rce.parser.IContract;
import com.rce.parser.OpenApiContract;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.ParseOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

class OpenApiContractTest {

    static String basePath = "./src/test/resources/";

    IContract contract;

    @BeforeEach
    void setUp() {
        ParseOptions parseOptions = new ParseOptions();
        parseOptions.setResolve(true);
        parseOptions.setResolveFully(true);

        OpenAPI api = new OpenAPIParser().readLocation(basePath + "contract.yml", null, parseOptions).getOpenAPI();

        this.contract = new OpenApiContract(api);
    }

    @Test
    void testGetEndpoints() {
        assertAgainstFileContent(getEndpoints(), Paths.get(basePath + "getEndpoints.txt"));
    }

    String getEndpoints() {
        StringBuilder stringBuilder = new StringBuilder();

        List<Endpoint> endpoints = new ArrayList<>(contract.getEndpoints());
        endpoints.sort(Comparator.comparing(Endpoint::toString));

        for (Endpoint endpoint : endpoints)
            stringBuilder.append(endpoint.toString()).append("\n");

        return stringBuilder.toString();
    }

    @Test
    void testGetRequestProperties() {
        assertAgainstFileContent(getRequestProperties(), Paths.get(basePath + "getRequestProperties.txt"));
    }

    String getRequestProperties() {
        StringBuilder stringBuilder = new StringBuilder();

        List<Endpoint> endpoints = new ArrayList<>(contract.getEndpoints());
        endpoints.sort(Comparator.comparing(Endpoint::toString));

        for (Endpoint endpoint : endpoints) {
            stringBuilder.append(endpoint.toString()).append("\n");

            List<Property> properties = new ArrayList<>(contract.getRequestProperties(endpoint));
            properties.sort(Comparator.comparing(Property::toString));

            for (Property p : properties)
                stringBuilder.append("\t").append(p.toString()).append("\n");

            stringBuilder.append("\n");
        }

        return stringBuilder.toString();
    }

    @Test
    void testGetResponses() {
        assertAgainstFileContent(getResponses(), Paths.get(basePath + "getResponses.txt"));
    }

    String getResponses() {
        StringBuilder stringBuilder = new StringBuilder();

        List<Endpoint> endpoints = new ArrayList<>(contract.getEndpoints());
        endpoints.sort(Comparator.comparing(Endpoint::toString));

        for (Endpoint endpoint : endpoints) {
            stringBuilder.append(endpoint.toString()).append("\n");

            List<String> responses = contract.getResponses(endpoint);
            responses.sort(Comparator.naturalOrder());

            for (String r : responses)
                stringBuilder.append("\t").append(r).append("\n");

            stringBuilder.append("\n");
        }

        return stringBuilder.toString();
    }

    @Test
    void testGetResponseProperties() {
        assertAgainstFileContent(getResponseProperties(), Paths.get(basePath + "getResponseProperties.txt"));
    }

    String getResponseProperties() {
        StringBuilder stringBuilder = new StringBuilder();

        List<Endpoint> endpoints = new ArrayList<>(contract.getEndpoints());
        endpoints.sort(Comparator.comparing(Endpoint::toString));

        for (Endpoint endpoint : endpoints) {
            stringBuilder.append(endpoint.toString()).append("\n");

            List<String> responses = contract.getResponses(endpoint);
            responses.sort(Comparator.naturalOrder());

            for (String response : responses) {
                stringBuilder.append("\t").append(response).append("\n");

                List<Property> properties = new ArrayList<>(contract.getResponseProperties(endpoint, response));
                properties.sort(Comparator.comparing(Property::toString));

                for (Property p : properties)
                    stringBuilder.append("\t\t").append(p.toString()).append("\n");
            }

            stringBuilder.append("\n");
        }

        return stringBuilder.toString();
    }

    static void assertAgainstFileContent(String content, Path filePath) {
        try {
            String fileContent = Files.readString(filePath, StandardCharsets.US_ASCII);
            assert fileContent.equals(content);
        } catch (IOException e) {
            System.err.println("Expected values where not found for test: " + filePath.getFileName().toString());
        }
    }
}