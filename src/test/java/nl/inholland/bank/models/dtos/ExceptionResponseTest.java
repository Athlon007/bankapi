package nl.inholland.bank.models.dtos;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

class ExceptionResponseTest {
    @Test
    void settingExceptionResponseShouldWork() {
        ExceptionResponse exceptionResponse = new ExceptionResponse("message");
        Assertions.assertEquals("message", exceptionResponse.error_message());
    }
}
