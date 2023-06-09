package nl.inholland.bank.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TokenTests {
    @Test
    void settingTokenRecordShouldWork() {
        Token token = new Token("token", 1000);
        Assertions.assertEquals("token", token.jwt());
    }
}
