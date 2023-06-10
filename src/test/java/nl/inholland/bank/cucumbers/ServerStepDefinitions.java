package nl.inholland.bank.cucumbers;

import com.jayway.jsonpath.JsonPath;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import org.junit.jupiter.api.Assertions;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.util.List;

public class ServerStepDefinitions extends BaseStepDefinitions {

    @Before
    public static void beforeEach() {
        StorageForTestsInstance.getInstance().setJwt(null);
        StorageForTestsInstance.getInstance().setResponse(null);
    }

    @Given("The endpoint for {string} is available for method {string}")
    public void theEndpointForIsAvailableForMethod(String endpoint, String method) {
        if (StorageForTestsInstance.getInstance().getJwt() != null) {
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(StorageForTestsInstance.getInstance().getJwt().access_token());
        }

        StorageForTestsInstance.getInstance().setResponse(restTemplate.exchange(
                "/" + endpoint,
                HttpMethod.OPTIONS,
                new HttpEntity<>(null, headers),
                String.class
        ));

        List<String> options = List.of(StorageForTestsInstance.getInstance().getResponse().getHeaders().get("Allow").get(0).replaceAll("]", "").split(","));
        System.out.println("Options: "+ options);
        Assertions.assertTrue(options.contains(method.toUpperCase()));
    }
    @And("I get HTTP status {int}")
    public void iGetHTTPStatus(int code) {
        Assertions.assertEquals(code, StorageForTestsInstance.getInstance().getResponse().getStatusCode().value());
    }

    @And("I get {int} elements in the list")
    public void iGetElementsInTheList(int count) {
        Assertions.assertEquals(count, (Integer) JsonPath.read(StorageForTestsInstance.getInstance().getResponse().getBody().toString(), "$.length()"));
    }
}
