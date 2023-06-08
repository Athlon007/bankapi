package nl.inholland.bank.cucumbers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.jsonwebtoken.lang.Assert;
import nl.inholland.bank.models.dtos.AccountDTO.AccountAbsoluteLimitRequest;
import nl.inholland.bank.models.dtos.AccountDTO.AccountRequest;
import nl.inholland.bank.models.dtos.AccountDTO.AccountResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Given("I call the application accounts end point with currencyType {string}, accountType {string}, userId {int}")
    public void iCallTheApplicationAccountsEndPointWithIBANCurrencyTypeAccountTypeUserId(String currencyType, String accountType, int userId) {
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (StorageForTestsInstance.getInstance().getJwt() != null) {
            headers.setBearerAuth(StorageForTestsInstance.getInstance().getJwt().access_token());
        }

        AccountRequest accountRequest = new AccountRequest(
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
    @And("I get account's currencyType {string} and accountType {string} and id {int}")
    public void iGetAnAccountSIBANAndCurrencyTypeAndAccountTypeAndId(String currencyType, String accountType, int id) throws JsonProcessingException {
        // get the account response
        AccountResponse accountResponse = objectMapper.readValue(
                StorageForTestsInstance.getInstance().getResponse().getBody().toString(),
                AccountResponse.class
        );

        Assert.isTrue(accountResponse.currency_type().equals(currencyType), "currencyType is " + accountResponse.currency_type());
        Assert.isTrue(accountResponse.account_type().equals(accountType), "accountType is " + accountResponse.account_type());
        Assert.isTrue(accountResponse.id() == id, "id is " + accountResponse.id());
    }

    @When("I call the application accounts end point with user id {int}")
    public void iCallTheApplicationAccountsEndPointWithUserId(int userId) {
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

    @Given("I call the application accounts end point with absoluteLimit {double} with userId {int}, accountId {int}")
    public void iCallTheApplicationAccountsEndPointWithAbsoluteLimitWithUserIdAccountId(double absoluteLimit, int userId, int accountId) {
        AccountAbsoluteLimitRequest accountAbsoluteLimitRequest = new AccountAbsoluteLimitRequest(
                absoluteLimit
        );

        headers.setContentType(MediaType.APPLICATION_JSON);

        if (StorageForTestsInstance.getInstance().getJwt() != null) {
            headers.setBearerAuth(StorageForTestsInstance.getInstance().getJwt().access_token());
        }

        StorageForTestsInstance.getInstance().setResponse(restTemplate.exchange(
                ACCOUNTS_ENDPOINT + "/" + userId + "/" + accountId + "/" + "limit",
                HttpMethod.PUT,
                new HttpEntity<>(accountAbsoluteLimitRequest, headers),
                String.class
        ));
    }
}
