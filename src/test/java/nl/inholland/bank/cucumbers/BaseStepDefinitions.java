package nl.inholland.bank.cucumbers;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
// Disable certificate validation for HTTPS requests
@CucumberContextConfiguration
public class BaseStepDefinitions {
    @Autowired
    protected TestRestTemplate restTemplate;

    protected ResponseEntity response;
}
