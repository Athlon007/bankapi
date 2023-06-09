package nl.inholland.bank.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

class AccountTest {
    private Account account;

    @BeforeEach
    public void setUp() {
       User user = new User(
                "John",
                "Doe",
                "mail@test.com",
                "111222333",
                "0612345678",
                LocalDate.of(1990, 1, 1),
                "username",
                "Password1!",
                Role.CUSTOMER);

       account = new Account(
                user,
                1000,
                CurrencyType.EURO,
                "NL32INHO3125817743",
                AccountType.CURRENT,
                0);
    }

    @Test
    public void checkID() {
        // Arrange
        int expectedID = 0;

        // Act
        int actualID = account.getId();

        // Assert
        Assertions.assertEquals(expectedID, actualID);
    }

    @Test
    public void setBalance_PositiveBalance_SetsBalance() {
        // Arrange
        double positiveBalance = 1000;

        // Act
        account.setBalance(positiveBalance);

        // Assert
        Assertions.assertEquals(positiveBalance, account.getBalance());
    }

    @Test
    public void balanceCannotBeNaN() {
        // Arrange
        double nanBalance = Double.NaN;

        // Act & Assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            account.setBalance(nanBalance);
        });
    }

    @Test
    public void setCurrencyType_NullCurrencyType_ThrowsIllegalArgumentException() {
        // Arrange
        CurrencyType nullCurrencyType = null;

        // Act & Assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            account.setCurrencyType(nullCurrencyType);
        });
    }

    @Test
    public void setCurrencyType_ValidCurrencyType_SetsCurrencyType() {
        // Arrange
        CurrencyType validCurrencyType = CurrencyType.EURO;

        // Act
        account.setCurrencyType(validCurrencyType);

        // Assert
        Assertions.assertEquals(validCurrencyType, account.getCurrencyType());
    }

    @Test
    public void setIBAN_NullIBAN_ThrowsIllegalArgumentException() {
        // Arrange
        String nullIBAN = null;

        // Act & Assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            account.setIBAN(nullIBAN);
        });
    }

    @Test
    public void setIBAN_ValidIBAN_SetsIBAN() {
        // Arrange
        String validIBAN = "NL32INHO3125817743";

        // Act
        account.setIBAN(validIBAN);

        // Assert
        Assertions.assertEquals(validIBAN, account.getIBAN());
    }


    @Test
    public void setIBAN_InvalidIBAN_ThrowsIllegalArgumentException() {
        // Arrange
        String invalidIBAN = "NL11";

        // Act & Assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            account.setIBAN(invalidIBAN);
        });
    }

    @Test
    public void setType_NullType_ThrowsIllegalArgumentException() {
        // Arrange
        AccountType nullType = null;

        // Act & Assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            account.setType(nullType);
        });
    }

    @Test
    public void setType_ValidType_SetsType() {
        // Arrange
        AccountType validType = AccountType.CURRENT;

        // Act
        account.setType(validType);

        // Assert
        Assertions.assertEquals(validType, account.getType());
    }

    @Test
    public void setActivity_ActiveAccount_SetsActivity() {
        // Arrange
        boolean active = true;

        // Act
        account.setActive(active);

        // Assert
        Assertions.assertEquals(active, account.isActive());
    }

    @Test
    public void setActivity_InactiveAccount_SetsActivity() {
        // Arrange
        boolean inactive = false;

        // Act
        account.setActive(inactive);

        // Assert
        Assertions.assertEquals(inactive, account.isActive());
    }

    @Test
    void settingAbsoluteLimitAbove0ThrowsIllegalArgumentException() {
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            account.setAbsoluteLimit(1000);
        });

        Assertions.assertEquals("Absolute limit cannot be higher than 0", exception.getMessage());
    }

    @Test
    void getSetAbsoluteLimit() {
        account.setAbsoluteLimit(-1);
        Assertions.assertEquals(-1, account.getAbsoluteLimit());
    }

    @Test
    void getSetUser() {
        User user = new User();
        account.setUser(user);
        Assertions.assertEquals(user, account.getUser());
    }
}
