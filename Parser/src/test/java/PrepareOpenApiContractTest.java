import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class PrepareOpenApiContractTest {

    public static void main(String[] args) throws IOException { //Generate logs
        OpenApiContractTest test = new OpenApiContractTest();

        test.setUp();

        String getEndpoints = test.getEndpoints();
        Files.writeString(
                Paths.get(OpenApiContractTest.basePath + "getEndpoints.txt"),
                getEndpoints,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
        );

        String getRequestProperties = test.getRequestProperties();
        Files.writeString(
                Paths.get(OpenApiContractTest.basePath + "getRequestProperties.txt"),
                getRequestProperties,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
        );

        String getResponses = test.getResponses();
        Files.writeString(
                Paths.get(OpenApiContractTest.basePath + "getResponses.txt"),
                getResponses,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
        );

        String getResponseProperties = test.getResponseProperties();
        Files.writeString(
                Paths.get(OpenApiContractTest.basePath + "getResponseProperties.txt"),
                getResponseProperties,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
        );
    }

}
