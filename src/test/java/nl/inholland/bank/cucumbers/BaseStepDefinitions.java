package nl.inholland.bank.cucumbers;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@CucumberContextConfiguration
public class BaseStepDefinitions {
    @Autowired
    protected TestRestTemplate restTemplate;
    protected HttpHeaders headers = new HttpHeaders();

    public static final String VALID_USERNAME = "admin";
    public static final String VALID_PASSWORD = "Password1!";
    public static final String INVALID_USERNAME = "brteabtea";
    public static final String INVALID_PASSWORD = "Password1gertabh!";

    public static final String CLIENT_USERNAME = "client";
    public static final String CLIENT_PASSWORD = "Password3!";
}
