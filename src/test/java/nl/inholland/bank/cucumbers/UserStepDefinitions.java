package nl.inholland.bank.cucumbers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import nl.inholland.bank.models.User;
import nl.inholland.bank.models.dtos.UserDTO.UserForAdminRequest;
import nl.inholland.bank.models.dtos.UserDTO.UserForClientResponse;
import nl.inholland.bank.models.dtos.UserDTO.UserRequest;
import nl.inholland.bank.models.dtos.UserDTO.UserResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

public class UserStepDefinitions extends BaseStepDefinitions {
    public static final String USERS_ENDPOINT = "/users";

    private ObjectMapper objectMapper = new ObjectMapper();

    @When("I call the application users endpoint")
    public void iCallTheApplicationUsersEndpoint() {
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (StorageForTestsInstance.getInstance().getJwt() != null) {
            headers.setBearerAuth(StorageForTestsInstance.getInstance().getJwt().access_token());
        }

        StorageForTestsInstance.getInstance().setResponse(restTemplate.exchange(
                USERS_ENDPOINT,
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                String.class
        ));
    }

    @And("I get a list of users")
    public void iGetAListOfUsers() throws JsonProcessingException {
        System.out.println("Response: " + StorageForTestsInstance.getInstance().getResponse().getBody());

        // Deserialize array of UserResponse objects
        List<UserResponse> userResponses = objectMapper.readValue(
                StorageForTestsInstance.getInstance().getResponse().getBody().toString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, UserResponse.class)
        );



        System.out.println("Users Responses:");
        System.out.println(userResponses);

        Assert.notEmpty(userResponses, "Users list is empty");
    }

    @And("I get a list of users for client")
    public void iGetAListOfUsersForClient() throws JsonProcessingException {
        System.out.println("Response: " + StorageForTestsInstance.getInstance().getResponse().getBody());

        // Deserialize array of UserResponse objects
        List<UserForClientResponse> userResponses = objectMapper.readValue(
                StorageForTestsInstance.getInstance().getResponse().getBody().toString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, UserForClientResponse.class)
        );



        System.out.println("Users Responses:");
        System.out.println(userResponses);

        Assert.notEmpty(userResponses, "Users list is empty");
    }

    @When("I call the application users endpoint with page {int} and limit {int}")
    public void iCallTheApplicationUsersEndpointWithPageAndLimit(int page, int limit) {
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (StorageForTestsInstance.getInstance().getJwt() != null) {
            headers.setBearerAuth(StorageForTestsInstance.getInstance().getJwt().access_token());
        }

        StorageForTestsInstance.getInstance().setResponse(restTemplate.exchange(
                USERS_ENDPOINT + "?page=" + page + "&limit=" + limit,
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                String.class
        ));
    }

    @When("I call the application users endpoint with name {string}")
    public void iCallTheApplicationUsersEndpointWithName(String name) {
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (StorageForTestsInstance.getInstance().getJwt() != null) {
            headers.setBearerAuth(StorageForTestsInstance.getInstance().getJwt().access_token());
        }

        StorageForTestsInstance.getInstance().setResponse(restTemplate.exchange(
                USERS_ENDPOINT + "?name=" + name,
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                String.class
        ));
    }

    @When("I call the application users endpoint with no accounts")
    public void iCallTheApplicationUsersEndpointWithNoAccounts() {
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (StorageForTestsInstance.getInstance().getJwt() != null) {
            headers.setBearerAuth(StorageForTestsInstance.getInstance().getJwt().access_token());
        }

        StorageForTestsInstance.getInstance().setResponse(restTemplate.exchange(
                USERS_ENDPOINT + "?has_no_accounts=true",
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                String.class
        ));
    }

    @And("First element first name is {string}")
    public void firstElementFirstNameIs(String firstName) {
        List<UserResponse> userResponses = new ArrayList<>();

        try {
            userResponses = objectMapper.readValue(
                    StorageForTestsInstance.getInstance().getResponse().getBody().toString(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, UserResponse.class)
            );
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        Assert.isTrue(userResponses.get(0).firstname().equals(firstName), "First name is not " + firstName);
    }

    @Given("I call the application register endpoint with username {string}, first name {string}, last name {string}, email {string}, password {string}, bsn {string}, phone number {string} and birth-date {string}")
    public void iCallTheApplicationRegisterEndpointWithFirstNameLastNameEmailPasswordBsnPhoneNumberAndBirthDate(String username, String firstName, String lastName, String email, String password, String bsn, String phoneNumber, String birthDate) {
        headers.setContentType(MediaType.APPLICATION_JSON);

        UserRequest user = new UserRequest(
                email,
                username,
                password,
                firstName,
                lastName,
                bsn,
                phoneNumber,
                birthDate
        );

        StorageForTestsInstance.getInstance().setResponse(restTemplate.exchange(
                USERS_ENDPOINT,
                HttpMethod.POST,
                new HttpEntity<>(user, headers),
                String.class
        ));
    }

    @And("I get a user with first name {string} and last name {string}")
    public void iGetAUserWithFirstNameAndLastName(String firstName, String lastName) throws JsonProcessingException {
        // Get UserResponse from response
        UserResponse userResponse = objectMapper.readValue(
                StorageForTestsInstance.getInstance().getResponse().getBody().toString(),
                UserResponse.class
        );
    }

    @Given("I call the application register endpoint with no body")
    public void iCallTheApplicationRegisterEndpointWithNoBody() {
        headers.setContentType(MediaType.APPLICATION_JSON);

        StorageForTestsInstance.getInstance().setResponse(restTemplate.exchange(
                USERS_ENDPOINT,
                HttpMethod.POST,
                new HttpEntity<>(null, headers),
                String.class
        ));
    }

    @Given("I call the application register endpoint with username {string}, first name {string}, last name {string}, email {string}, password {string}, bsn {string}, phone number {string}, birth-date {string}, and role {string}")
    public void iCallTheApplicationRegisterEndpointWithUsernameFirstNameLastNameEmailPasswordBsnPhoneNumberBirthDateAndRole(String username, String firstName, String lastName, String email, String password, String bsn, String phoneNumber, String birthDate, String role) {
        headers.setContentType(MediaType.APPLICATION_JSON);

        UserForAdminRequest user = new UserForAdminRequest(
                email,
                username,
                password,
                firstName,
                lastName,
                bsn,
                phoneNumber,
                birthDate,
                role
        );

        if (StorageForTestsInstance.getInstance().getJwt() != null) {
            headers.setBearerAuth(StorageForTestsInstance.getInstance().getJwt().access_token());
        }

        StorageForTestsInstance.getInstance().setResponse(restTemplate.exchange(
                USERS_ENDPOINT,
                HttpMethod.POST,
                new HttpEntity<>(user, headers),
                String.class
        ));
    }

    @And("The user has role of {string}")
    public void theUserHasRoleOf(String role) throws JsonProcessingException {
        UserResponse userResponse = objectMapper.readValue(
                StorageForTestsInstance.getInstance().getResponse().getBody().toString(),
                UserResponse.class
        );

        Assert.isTrue(userResponse.role().equalsIgnoreCase(role), "User role is not " + role + ". User role is: " + userResponse.role());
    }
}
