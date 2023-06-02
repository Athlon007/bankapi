package nl.inholland.bank.cucumbers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import nl.inholland.bank.models.dtos.UserDTO.UserLimitsRequest;
import nl.inholland.bank.models.dtos.UserDTO.UserLimitsResponse;
import org.junit.jupiter.api.Assertions;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

public class UserLimitStepDefinitions extends BaseStepDefinitions {
    public static final String USERS_LIMITS_ENDPOINT = "/users/{userId}/limits";

    private ObjectMapper objectMapper = new ObjectMapper();

    @When("I get user limits for user {int}")
    public void iGetUserLimitsForUser(int userId) {
        if (StorageForTestsInstance.getInstance().getJwt() != null) {
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(StorageForTestsInstance.getInstance().getJwt().access_token());
        }

        StorageForTestsInstance.getInstance().setResponse(restTemplate.exchange(
                USERS_LIMITS_ENDPOINT.replace("{userId}", String.valueOf(userId)),
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                String.class
        ));
    }

    @And("I get valid user limits schema")
    public void iGetValidUserLimitsSchema() throws JsonProcessingException {
        UserLimitsResponse userLimitsResponse = objectMapper.readValue(
                StorageForTestsInstance.getInstance().getResponse().getBody().toString(),
                UserLimitsResponse.class
        );

        assert userLimitsResponse != null;
    }

    @When("I update user id {int} limits to transaction limit {int}, daily limit {int} and absolute limit {int}")
    public void iUpdateMyLimitsToTransactionLimitDailyLimitAndAbsoluteLimit(int id, int transactionLimit, int dailyLimit, int absoluteLimit) {
        UserLimitsRequest userLimitsRequest = new UserLimitsRequest(transactionLimit, dailyLimit, absoluteLimit);

        if (StorageForTestsInstance.getInstance().getJwt() != null) {
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(StorageForTestsInstance.getInstance().getJwt().access_token());
        }

        StorageForTestsInstance.getInstance().setResponse(restTemplate.exchange(
                USERS_LIMITS_ENDPOINT.replace("{userId}", String.valueOf(id)),
                HttpMethod.PUT,
                new HttpEntity<>(userLimitsRequest, headers),
                String.class
        ));
    }

    @And("I get a response with the following body with transaction limit {int}, daily limit {int}, absolute limit {int} and remaining daily limit {int}")
    public void iGetAResponseWithTheFollowingBodyWithTransactionLimitDailyLimitAbsoluteLimitAndRemainingDailyLimit(
            int transactionLimit,
            int dailyLimit,
            int absoluteLimit,
            int remainingDailyLimit
    ) throws JsonProcessingException {
        UserLimitsResponse userLimitsResponse = objectMapper.readValue(
                StorageForTestsInstance.getInstance().getResponse().getBody().toString(),
                UserLimitsResponse.class
        );

        Assertions.assertEquals(transactionLimit, userLimitsResponse.transaction_limit());
        Assertions.assertEquals(dailyLimit, userLimitsResponse.daily_transaction_limit());
        Assertions.assertEquals(absoluteLimit, userLimitsResponse.absolute_limit());
        Assertions.assertEquals(remainingDailyLimit, userLimitsResponse.remaining_daily_transaction_limit());
    }
}
