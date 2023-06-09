package nl.inholland.bank.models.dtos;

import nl.inholland.bank.models.dtos.AuthDTO.LoginRequest;
import nl.inholland.bank.models.dtos.AuthDTO.RefreshTokenRequest;
import nl.inholland.bank.models.dtos.AuthDTO.jwt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AuthDTOsTests {
    @Test
    void testJwt() {
        jwt jwt = new jwt("token", "refresh", 1, 100000);
        Assertions.assertEquals("token", jwt.access_token());
    }

    @Test
    void testLoginRequest() {
        LoginRequest loginRequest = new LoginRequest("username", "password");
        Assertions.assertEquals("username", loginRequest.username());
    }

    @Test
    void testRefreshRequest() {
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest("refresh");
        Assertions.assertEquals("refresh", refreshRequest.refresh_token());
    }
}
