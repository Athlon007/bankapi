package nl.inholland.bank;

import nl.inholland.bank.models.Limits;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UserLimitsTests {
    private Limits limits;

    @BeforeEach
    public void setUp() {
        limits = new Limits();
        limits.setTransactionLimit(10000);
        limits.setDailyTransactionLimit(1000);
        limits.setAbsoluteLimit(0);
    }

    @Test
    void settingAbsoluteLimitAbove0ThrowsIllegalArgumentException() {
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            limits.setAbsoluteLimit(1000);
        });

        Assertions.assertEquals("Absolute limit cannot be higher than 0", exception.getMessage());
    }

    @Test
    void settingTransactionLimitBelow0ThrowsIllegalArgumentException() {
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            limits.setTransactionLimit(-1000);
        });

        Assertions.assertEquals("Transaction limit cannot be lower than 0", exception.getMessage());
    }

    @Test
    void settingDailyTransactionLimitBelow0ThrowsIllegalArgumentException() {
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            limits.setDailyTransactionLimit(-1000);
        });

        Assertions.assertEquals("Daily transaction limit cannot be lower than 0", exception.getMessage());
    }
}
