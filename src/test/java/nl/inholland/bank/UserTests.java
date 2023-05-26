package nl.inholland.bank;

import nl.inholland.bank.models.Account;
import nl.inholland.bank.models.AccountType;
import nl.inholland.bank.models.Role;
import nl.inholland.bank.models.User;
import nl.inholland.bank.models.exceptions.OperationNotAllowedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest
class UserTests {
    private User user;

    @BeforeEach
    void setUp() {
        user = new User(
                "John",
                "Doe",
                "mail@test.com",
                "111222333",
                "0612345678",
                LocalDate.of(1990, 1, 1),
                "username",
                "Password1!",
                Role.USER);

        Account currentAccount = new Account();
        currentAccount.setBalance(1000);
        currentAccount.setIBAN("NL01INHO0000000001");
        currentAccount.setType(AccountType.CURRENT);
        currentAccount.setActive(true);
        user.setCurrentAccount(currentAccount);
    }

    @Test
    void bsnMustAlwaysBe8Or9Digits() {
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            user.setBsn("1234567890");
        });

        Assertions.assertEquals("BSN must be 8 or 9 digits long", exception.getMessage());

        // Check if setting correct one does NOT throw an exception
        user.setBsn("12345678");
        Assertions.assertEquals("12345678", user.getBsn());
    }

    @Test
    void bsnShouldOnlyContainNumbers() {
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            user.setBsn("1234567a");
        });

        Assertions.assertEquals("BSN must only contain numbers", exception.getMessage());
    }

    @Test
    void phoneNumberShouldOnlyContainNumbers() {
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            user.setPhoneNumber("061234567a");
        });

        Assertions.assertEquals("Phone number must only contain numbers", exception.getMessage());
    }

    @Test
    void localPhoneNumbersCannotBeLongerThan10Digits() {
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            user.setPhoneNumber("06123456789");
        });

        Assertions.assertEquals("Phone number must be 9 or 10 digits long", exception.getMessage());
    }

    @Test
    void emailShouldBeValid() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            user.setEmail("test");
        });

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            user.setEmail("test@test");
        });

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            user.setEmail("test@test@com");
        });

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            user.setEmail("test@test.");
        });

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            user.setEmail("test@.com");
        });

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            user.setEmail("@test.com");
        });

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            user.setEmail(".com");
        });

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            user.setEmail("@");
        });

        // Valid test.
        user.setEmail("test@example.com");
        Assertions.assertEquals("test@example.com", user.getEmail());
        user.setEmail("my.test@example.com");
        Assertions.assertEquals("my.test@example.com", user.getEmail());
    }

    @Test
    void birthdateShouldNotBeInTheFuture() {
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            user.setDateOfBirth(LocalDate.now().plusDays(1));
        });

        Assertions.assertEquals("Date of birth cannot be in the future", exception.getMessage());
    }

    @Test
    void settingRoleToNullThrowsIllegalArgumentException() {
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            user.setRole(null);
        });

        Assertions.assertEquals("Role cannot be null", exception.getMessage());
    }

    @Test
    void usernameCannotBeNullOrEmpty()
    {
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            user.setUsername(null);
        });

        Assertions.assertEquals("Username cannot be null or empty", exception.getMessage());

        exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            user.setUsername("");
        });

        Assertions.assertEquals("Username cannot be null or empty", exception.getMessage());
    }

    @Test
    void setttingLastNameToNullShouldReplaceWithEmptyString() {
        user.setLastName(null);
        Assertions.assertEquals("", user.getLastName());
    }

    @Test
    void attemptingToOverwriteCurrentAccountShouldThrowOperationNotAllowedException() {
        Exception exception = Assertions.assertThrows(OperationNotAllowedException.class, () -> {
            Account account = new Account();
            account.setBalance(1000);
            account.setIBAN("NL01INHO0000000001");
            account.setType(AccountType.CURRENT);
            account.setActive(true);
            user.setCurrentAccount(account);
        });

        Assertions.assertEquals("User already has a current account", exception.getMessage());
    }

    @Test
    void userCannotUnbindCurrentAccount() {
        Exception exception = Assertions.assertThrows(OperationNotAllowedException.class, () -> {
            user.setCurrentAccount(null);
        });

        Assertions.assertEquals("Cannot unbind current account", exception.getMessage());
    }

    @Test
    void attemptingToSetSavingsAccountInCurrentsAccountPlaceShouldThrowIllegalArgumentException() {
        // We need a new User object for this one...
        user = new User();
        user.setId(1);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("totallyrealemail@notascam.com");

        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Account account = new Account();
            account.setBalance(1000);
            account.setIBAN("NL01INHO0000000001");
            account.setType(AccountType.SAVING);
            account.setActive(true);
            user.setCurrentAccount(account);
        });

        Assertions.assertEquals("Account type must be CURRENT", exception.getMessage());
    }

    @Test
    void attemptingToSetCurrentAccountInPlaceOfSavingAccountShouldThrowIllegalArgumentException() {
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Account account = new Account();
            account.setBalance(1000);
            account.setIBAN("NL01INHO0000000001");
            account.setType(AccountType.CURRENT);
            account.setActive(true);
            user.setSavingAccount(account);
        });

        Assertions.assertEquals("Account type must be SAVING", exception.getMessage());

        // Test positive case
        Account account = new Account();
        account.setBalance(1000);
        account.setIBAN("NL01INHO0000000001");
        account.setType(AccountType.SAVING);
        account.setActive(true);
        user.setSavingAccount(account);

        Assertions.assertEquals(account, user.getSavingAccount());
    }

    @Test
    void userCannotRemoveSavingAccountIfItHasABalance() {
        Exception exception = Assertions.assertThrows(OperationNotAllowedException.class, () -> {
            Account account = new Account();
            account.setBalance(1000);
            account.setIBAN("NL01INHO0000000001");
            account.setType(AccountType.SAVING);
            account.setActive(true);
            user.setSavingAccount(account);


            user.setSavingAccount(null);
        });

        Assertions.assertEquals("Cannot remove saving account with balance", exception.getMessage());
    }

    @Test
    void userCannotSetSavingAccountIfHeDoesNotHaveCurrentAccount() {
        user = new User();
        user.setId(1);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("totallyrealemail@notascam.com");

        Exception exception = Assertions.assertThrows(OperationNotAllowedException.class, () -> {
            Account account = new Account();
            account.setBalance(1000);
            account.setIBAN("NL01INHO0000000001");
            account.setType(AccountType.SAVING);
            account.setActive(true);
            user.setSavingAccount(account);
        });

        Assertions.assertEquals("Cannot set saving account without current account", exception.getMessage());
    }

    @Test
    void bsnMustPass11Test() {
        // Try setting falsey BSN: 192837465
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            user.setBsn("192837465");
        });

        // Try setting correct BSN: 123456782
        user.setBsn("123456782");
        Assertions.assertEquals("123456782", user.getBsn());

        user.setBsn("10464554");
        Assertions.assertEquals("10464554", user.getBsn());
    }
}
