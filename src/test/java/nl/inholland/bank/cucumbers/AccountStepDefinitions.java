package nl.inholland.bank.cucumbers;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.util.List;

public class AccountStepDefinitions extends BaseStepDefinitions {
    private int userId;

    @Given("The endpoint {string} is available for the {string} method")
    public void theEndpointIsAvailableForTheMethod(String endpoint, String method) {
        response = restTemplate.exchange(
                "/" + endpoint,
                HttpMethod.OPTIONS,
                new HttpEntity<>(null, new HttpHeaders()),
                String.class
        );

        List<String> options = List.of(response.getHeaders().get("Allow").get(0).replaceAll("]", "").split(","));
        Assertions.assertTrue(options.contains(method.toUpperCase()));
    }

    @When("I retrieve all accounts for user with ID {integer}")
    public void iRetrieveAllAccountsForUserWithID(int userId) {
        this.userId = USER_ID; // Store the user ID for later use
        String endpoint = "/accounts/" + userId;
        response = restTemplate.exchange(
                endpoint,
                HttpMethod.GET,
                new HttpEntity<>(null, new HttpHeaders()),
                String.class
        );
    }

    @And("I get HTTP status {int}")
    public void iGetHTTPStatus(int code) {
        Assertions.assertEquals(code, response.getStatusCode().value());
    }
}
