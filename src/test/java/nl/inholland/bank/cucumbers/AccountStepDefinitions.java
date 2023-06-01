package nl.inholland.bank.cucumbers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.jsonwebtoken.lang.Assert;
import nl.inholland.bank.models.dtos.AccountDTO.AccountRequest;
import nl.inholland.bank.models.dtos.AccountDTO.AccountResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.util.List;

public class AccountStepDefinitions extends BaseStepDefinitions{

    public static final String ACCOUNTS_ENDPOINT = "/accounts";

    private ObjectMapper objectMapper = new ObjectMapper();

    @When("I call the application accounts endpoint with user id {int}")
    public void iCallTheApplicationAccountsEndpointWithUserId(int userId) {
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (StorageForTestsInstance.getInstance().getJwt() != null) {
            headers.setBearerAuth(StorageForTestsInstance.getInstance().getJwt().access_token());
        }

        StorageForTestsInstance.getInstance().setResponse(restTemplate.exchange(
                ACCOUNTS_ENDPOINT + "/" + userId,
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                String.class
        ));
    }

    @Then("I get HTTP status code {int}")
    public void iGetHTTPStatusCode(int expectedStatusCode) {
        System.out.println("Response: " + StorageForTestsInstance.getInstance().getResponse().getBody());
        Assert.isTrue(StorageForTestsInstance.getInstance().getResponse().getStatusCode().value() == expectedStatusCode,
                "Status code is not " + expectedStatusCode);
    }
    

    @Given("I call the application accounts end point with IBAN {string}, currencyType {string}, accountType {string}, userId {int}")
    public void iCallTheApplicationAccountsEndPointWithIBANCurrencyTypeAccountTypeUserId(String IBAN, String currencyType, String accountType, int userId) {
        headers.setContentType(MediaType.APPLICATION_JSON);

        AccountRequest accountRequest = new AccountRequest(
                IBAN,
                currencyType,
                accountType,
                userId
        );

        StorageForTestsInstance.getInstance().setResponse(restTemplate.exchange(
                ACCOUNTS_ENDPOINT,
                HttpMethod.POST,
                new HttpEntity<>(accountRequest, headers),
                String.class
        ));
    }


    @And("I get an account's IBAN {string}")
    public void iGetAnAccountSIBAN(String IBAN) {
        // get the account response
        AccountResponse accountResponse = objectMapper.convertValue(
                StorageForTestsInstance.getInstance().getResponse().getBody(),
                AccountResponse.class
        );

        Assert.isTrue(accountResponse.IBAN().equals(IBAN), "IBAN is " + IBAN);
    }
}
