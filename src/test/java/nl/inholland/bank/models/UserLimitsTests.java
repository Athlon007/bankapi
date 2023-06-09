package nl.inholland.bank.models;

import nl.inholland.bank.models.Limits;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

class UserLimitsTests {
    private Limits limits;

    @BeforeEach
    public void setUp() {
        limits = new Limits();
        limits.setTransactionLimit(10000);
        limits.setDailyTransactionLimit(1000);
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

    @Test
    void settingRemainingDailyTransactionLimitsBelowZeroShouldSwitchItToZero() {
        limits.setRemainingDailyTransactionLimit(-1000);
        Assertions.assertEquals(0, limits.getRemainingDailyTransactionLimit());
    }

    @Test
    void settingUserShouldWork() {
        User user = new User();
        limits.setUser(user);
        Assertions.assertEquals(user, limits.getUser());
    }

    @Test
    void getSetId() {
        limits.setId(1);
        Assertions.assertEquals(1, limits.getId());
    }

    @Test
    void getSetTransactionLimit() {
        limits.setTransactionLimit(1);
        Assertions.assertEquals(1, limits.getTransactionLimit());
    }

    @Test
    void getSetDailyTransactionLimit() {
        limits.setDailyTransactionLimit(1);
        Assertions.assertEquals(1, limits.getDailyTransactionLimit());
    }
}
