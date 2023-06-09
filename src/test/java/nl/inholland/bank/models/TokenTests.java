package nl.inholland.bank.models;

import nl.inholland.bank.models.dtos.Token;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TokenTests {
    @Test
    void settingTokenRecordShouldWork() {
        Token token = new Token("token", 1000);
        Assertions.assertEquals("token", token.jwt());
    }
}
