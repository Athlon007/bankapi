package nl.inholland.bank.models.dtos;

import nl.inholland.bank.models.dtos.AccountDTO.AccountResponse;
import nl.inholland.bank.models.dtos.UserDTO.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UserDTOsTests {
    @Test
    void settingUserResponse() {
        UserResponse userResponse = new UserResponse(1, "username", "email", "username", "lastname", "1234", "phone", "2000", 0d, "role", new AccountResponse(0, "bian", "eur", "a", false, 0, 0, "firstName", "lastName"), null, true);
        Assertions.assertEquals("username", userResponse.firstname());
        Assertions.assertEquals("email", userResponse.email());
        Assertions.assertEquals("role", userResponse.role());
    }

    @Test
    void userRequestShouldWork() {
        UserRequest userRequest = new UserRequest("email", "username", "password", "firstname", "lastname", "1234", "phone", "2000");
        Assertions.assertEquals("email", userRequest.getEmail());
        Assertions.assertEquals("username", userRequest.getUsername());
        Assertions.assertEquals("password", userRequest.getPassword());
        Assertions.assertEquals("firstname", userRequest.getFirstname());
        Assertions.assertEquals("lastname", userRequest.getLastname());
        Assertions.assertEquals("phone", userRequest.getPhone_number());
        Assertions.assertEquals("2000", userRequest.getBirth_date());
        Assertions.assertEquals("1234", userRequest.getBsn());

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
        Assertions.assertEquals("email", userRequest.getEmail());
        Assertions.assertEquals("username", userRequest.getUsername());
        Assertions.assertEquals("password", userRequest.getPassword());
        Assertions.assertEquals("firstname", userRequest.getFirstname());
        Assertions.assertEquals("lastname", userRequest.getLastname());
        Assertions.assertEquals("phone", userRequest.getPhone_number());
        Assertions.assertEquals("2000", userRequest.getBirth_date());
        Assertions.assertEquals("1234", userRequest.getBsn());

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
        Assertions.assertEquals(1, userResponse.id());
        Assertions.assertEquals("firstname", userResponse.firstname());
        Assertions.assertEquals("lastname", userResponse.lastname());
        Assertions.assertEquals("1234", userResponse.iban());
    }

    @Test
    void userLimitsRequestShouldWork() {
        UserLimitsRequest userLimitsRequest = new UserLimitsRequest(1, 1);
        Assertions.assertEquals(1, userLimitsRequest.transaction_limit());
        Assertions.assertEquals(1, userLimitsRequest.daily_transaction_limit());
    }

    @Test
    void userLimitResponseShouldWork() {
        UserLimitsResponse userLimitResponse = new UserLimitsResponse(1, 1, 1);
        Assertions.assertEquals(1, userLimitResponse.transaction_limit());
        Assertions.assertEquals(1, userLimitResponse.daily_transaction_limit());
        Assertions.assertEquals(1, userLimitResponse.remaining_daily_transaction_limit());
    }

    @Test
    void compareTwoUserForAdminRequest() {
        UserForAdminRequest userRequest = new UserForAdminRequest("email", "username", "password", "firstname", "lastname", "1234", "phone", "2000", "role");
        UserForAdminRequest userRequest2 = new UserForAdminRequest("email", "username", "password", "firstname", "lastname", "1234", "phone", "2000", "role");
        Assertions.assertEquals(userRequest2, userRequest);
    }

    @Test
    void compareOtherObjectWithUserForAdminRequest() {
        UserForAdminRequest userRequest = new UserForAdminRequest("email", "username", "password", "firstname", "lastname", "1234", "phone", "2000", "role");
        Assertions.assertNotEquals("test", userRequest);
    }

    @Test
    void getHashCode() {
        UserForAdminRequest userRequest = new UserForAdminRequest("email", "username", "password", "firstname", "lastname", "1234", "phone", "2000", "role");
        Assertions.assertEquals(-163770035, userRequest.hashCode());
    }

    @Test
    void userForAdminRequestCompareOther() {
        UserForAdminRequest userRequest = new UserForAdminRequest("email", "username", "password", "firstname", "lastname", "1234", "phone", "2000", "role");
        Assertions.assertDoesNotThrow(() -> userRequest.equals(new Object()));
    }
}
