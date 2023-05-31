package nl.inholland.bank.cucumbers;

import com.jayway.jsonpath.JsonPath;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.util.Assert;

import java.util.List;

public class ServerStepDefinitions extends BaseStepDefinitions {
    @Given("The endpoint for {string} is available for method {string}")
    public void theEndpointForIsAvailableForMethod(String endpoint, String method) {
        response = restTemplate.exchange(
                "/" + endpoint,
                HttpMethod.OPTIONS,
                new HttpEntity<>(null, new HttpHeaders()),
                String.class
        );

        List<String> options = List.of(response.getHeaders().get("Allow").get(0).replaceAll("]", "").split(","));
        Assertions.assertTrue(options.contains(method.toUpperCase()));
    }
    @And("I get HTTP status {int}")
    public void iGetHTTPStatus(int code) {
        Assertions.assertEquals(code, response.getStatusCode().value());
    }
}
