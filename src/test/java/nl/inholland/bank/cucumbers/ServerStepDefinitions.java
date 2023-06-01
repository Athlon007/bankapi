package nl.inholland.bank.cucumbers;

import com.jayway.jsonpath.JsonPath;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.util.Assert;

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

        // print headers
        System.out.println(headers);

        StorageForTestsInstance.getInstance().setResponse(restTemplate.exchange(
                "/" + endpoint,
                HttpMethod.OPTIONS,
                new HttpEntity<>(null, headers),
                String.class
        ));

        System.out.println(StorageForTestsInstance.getInstance().getResponse().getHeaders().get("Allow").get(0).replaceAll("]", "").split(","));

        List<String> options = List.of(StorageForTestsInstance.getInstance().getResponse().getHeaders().get("Allow").get(0).replaceAll("]", "").split(","));
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
