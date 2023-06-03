package nl.inholland.bank.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Assertions;

@SpringBootTest
class BlacklistedRefreshTokenTests {

    private BlacklistedRefreshToken blacklistedRefreshToken;

    @BeforeEach
    public void setUp() {
        blacklistedRefreshToken = new BlacklistedRefreshToken();
        blacklistedRefreshToken.setToken("token");
    }

    @Test
    void settingTokenShouldWork() {
        blacklistedRefreshToken.setToken("token");
        Assertions.assertEquals("token", blacklistedRefreshToken.getToken());

        blacklistedRefreshToken = new BlacklistedRefreshToken("token");
        Assertions.assertEquals("token", blacklistedRefreshToken.getToken());
    }

    @Test
    void settingAndGettingIdShouldWork() {
        blacklistedRefreshToken.setId(1);
        Assertions.assertEquals(1, blacklistedRefreshToken.getId());
    }
}
