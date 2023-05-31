package nl.inholland.bank.cucumbers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nl.inholland.bank.models.dtos.AuthDTO.LoginRequest;
import nl.inholland.bank.models.dtos.AuthDTO.RefreshTokenRequest;
import nl.inholland.bank.models.dtos.AuthDTO.jwt;
import org.junit.jupiter.api.Assertions;
import org.springframework.http.*;

public class AuthenticationStepDefinitions extends BaseStepDefinitions {
    private ObjectMapper objectMapper = new ObjectMapper();

    private LoginRequest loginRequest;

    public static final String LOGIN_ENDPOINT = "/auth/login";

    private jwt jwt;

    @Given("I have a valid login credentials")
    public void iHaveAValidLoginCredentials() {
        loginRequest = new LoginRequest(VALID_USERNAME, VALID_PASSWORD);
    }

    @When("I call the application login endpoint")
    public void iCallTheApplicationLoginEndpoint() {
        headers.setContentType(MediaType.APPLICATION_JSON);
        response = restTemplate.exchange(
                LOGIN_ENDPOINT,
                HttpMethod.POST,
                new HttpEntity<>(loginRequest, headers),
                String.class
        );
    }

    @Then("I receive a token")
    public void iReceiveAToken() throws JsonProcessingException {
        jwt = objectMapper.readValue(response.getBody().toString(), jwt.class);
    }

    @Then("I receive unauthorized error")
    public void iReceiveUnauthorizedError() {
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Given("I have a valid username but invalid password")
    public void iHaveAValidUsernameButInvalidPassword() {
        loginRequest = new LoginRequest(VALID_USERNAME, INVALID_PASSWORD);
    }

    @Given("I have an invalid login credentials")
    public void iHaveAnInvalidLoginCredentials() {
        loginRequest = new LoginRequest(INVALID_USERNAME, INVALID_PASSWORD);
    }

    @When("I call the application refresh token endpoint")
    public void iCallTheApplicationRefreshTokenEndpoint() {
        headers.setContentType(MediaType.APPLICATION_JSON);
        response = restTemplate.exchange(
                "/auth/refresh",
                HttpMethod.POST,
                new HttpEntity<>(new RefreshTokenRequest(jwt.refresh_token()), headers),
                String.class
        );
    }
}
