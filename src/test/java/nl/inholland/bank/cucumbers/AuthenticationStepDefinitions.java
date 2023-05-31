package nl.inholland.bank.cucumbers;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import nl.inholland.bank.models.dtos.AuthDTO.LoginRequest;
import org.junit.jupiter.api.Assertions;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

public class AuthenticationStepDefinitions extends BaseStepDefinitions {

    @When("I login with username {string} and password {string}")
    public void iLoginWithUsernameAndPassword(String username, String password) {
        response = restTemplate.exchange(
                "/auth/login",
                HttpMethod.POST,
                new HttpEntity<>(new LoginRequest(username, password), new HttpHeaders()),
                String.class
        );

        System.out.println("==== Response ====");
        System.out.println(response);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
    }
}
