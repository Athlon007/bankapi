package nl.inholland.bank.models.dtos;

import nl.inholland.bank.models.dtos.AccountDTO.AccountResponse;
import nl.inholland.bank.models.dtos.UserDTO.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UserDTOsTests {
    @Test
    void settingUserResponse() {
        UserResponse userResponse = new UserResponse(1, "username", "email", "username", "lastname", "1234", "phone", "2000", 0d, "role", new AccountResponse(0, "bian", "eur", "a", false, 0, 0, "firstName", "lastName"), null, true);
        assert userResponse.firstname().equals("username");
        assert userResponse.email().equals("email");
        assert userResponse.role().equals("role");
    }

    @Test
    void userRequestShouldWork() {
        UserRequest userRequest = new UserRequest("email", "username", "password", "firstname", "lastname", "1234", "phone", "2000");
        assert userRequest.getEmail().equals("email");
        assert userRequest.getUsername().equals("username");
        assert userRequest.getPassword().equals("password");
        assert userRequest.getFirstname().equals("firstname");
        assert userRequest.getLastname().equals("lastname");
        assert userRequest.getPhone_number().equals("phone");
        assert userRequest.getBirth_date().equals("2000");
        assert userRequest.getBsn().equals("1234");

        // use set
        userRequest.setEmail("email2");
        userRequest.setUsername("username2");
        userRequest.setPassword("password2");
        userRequest.setFirstname("firstname2");
        userRequest.setLastname("lastname2");
        userRequest.setPhone_number("phone2");
        userRequest.setBirth_date("2002");
        userRequest.setBsn("12345");
    }

    @Test
    void userForAdminRequestShouldWork() {
        UserForAdminRequest userRequest = new UserForAdminRequest("email", "username", "password", "firstname", "lastname", "1234", "phone", "2000", "role");
        assert userRequest.getEmail().equals("email");
        assert userRequest.getUsername().equals("username");
        assert userRequest.getPassword().equals("password");
        assert userRequest.getFirstname().equals("firstname");
        assert userRequest.getLastname().equals("lastname");
        assert userRequest.getPhone_number().equals("phone");
        assert userRequest.getBirth_date().equals("2000");
        assert userRequest.getRole().equals("role");

        // use set
        userRequest.setEmail("email2");
        userRequest.setUsername("username2");
        userRequest.setPassword("password2");
        userRequest.setFirstname("firstname2");
        userRequest.setLastname("lastname2");
        userRequest.setPhone_number("phone2");
        userRequest.setBirth_date("2002");
        userRequest.setRole("role2");
    }

    @Test
    void userForClientResponseShouldWork() {
        UserForClientResponse userResponse = new UserForClientResponse(1, "firstname", "lastname", "1234");
        assert userResponse.id() == 1;
        assert userResponse.firstname().equals("firstname");
        assert userResponse.lastname().equals("lastname");
        assert userResponse.iban().equals("1234");
    }

    @Test
    void userLimitsRequestShouldWork() {
        UserLimitsRequest userLimitsRequest = new UserLimitsRequest(1, 1);
        assert userLimitsRequest.transaction_limit() == 1;
        assert userLimitsRequest.daily_transaction_limit() == 1;
    }

    @Test
    void userLimitResponseShouldWork() {
        UserLimitsResponse userLimitResponse = new UserLimitsResponse(1, 1, 1);
        assert userLimitResponse.transaction_limit() == 1;
        assert userLimitResponse.daily_transaction_limit() == 1;
        assert userLimitResponse.remaining_daily_transaction_limit() == 1;
    }

    @Test
    void compareTwoUserForAdminRequest() {
        UserForAdminRequest userRequest = new UserForAdminRequest("email", "username", "password", "firstname", "lastname", "1234", "phone", "2000", "role");
        UserForAdminRequest userRequest2 = new UserForAdminRequest("email", "username", "password", "firstname", "lastname", "1234", "phone", "2000", "role");
        assert userRequest.equals(userRequest2);
    }

    @Test
    void compareOtherObjectWithUserForAdminRequest() {
        UserForAdminRequest userRequest = new UserForAdminRequest("email", "username", "password", "firstname", "lastname", "1234", "phone", "2000", "role");
        assert !userRequest.equals("test");
    }

    @Test
    void getHashCode() {
        UserForAdminRequest userRequest = new UserForAdminRequest("email", "username", "password", "firstname", "lastname", "1234", "phone", "2000", "role");
        Assertions.assertEquals(-163770035, userRequest.hashCode());
    }
}
