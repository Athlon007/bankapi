package nl.inholland.bank.models;

import nl.inholland.bank.models.exceptions.OperationNotAllowedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

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
                Role.CUSTOMER);

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
            user.setBsn("0750600");
        });

        Assertions.assertEquals("BSN must be 8 or 9 digits long", exception.getMessage());

        // Check if setting correct one does NOT throw an exception
        user.setBsn("075060097");
        Assertions.assertEquals("075060097", user.getBsn());
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

    @Test
    void firstNameCannotBeNullOrEmpty() {
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            user.setFirstName(null);
        });

        Assertions.assertEquals("First name cannot be empty", exception.getMessage());

        exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            user.setFirstName("");
        });

        Assertions.assertEquals("First name cannot be empty", exception.getMessage());
    }

    @Test
    void gettingTotalBalanceReturnsADouble() {
        Assertions.assertEquals(1000, user.getTotalBalance());

        //Try adding a savings account
        Account account = new Account();
        account.setBalance(500);
        account.setType(AccountType.SAVING);

        user.setSavingAccount(account);

        Assertions.assertEquals(1500, user.getTotalBalance());
    }

    @Test
    void settingLimitsToNullThrowsIllegalArgumentException() {
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            user.setLimits(null);
        });

        Assertions.assertEquals("Limits cannot be null", exception.getMessage());
    }

    @Test
    void getAuthorityFromRole() {
        Assertions.assertEquals("CUSTOMER", user.getRole().getAuthority());
    }

    @Test
    void getPasswordReturnsPassword() {
        Assertions.assertEquals("Password1!", user.getPassword());
    }

    @Test
    void addingSavingAccountWhenUserHasDeactivatedCurrentAccountResultsInOperationNotAllowedException() {
        Account savingAccount = new Account();
        savingAccount.setType(AccountType.SAVING);

        Exception exception = Assertions.assertThrows(OperationNotAllowedException.class, () -> {
            user.getCurrentAccount().setActive(false);
            user.setSavingAccount(savingAccount);
        });

        Assertions.assertEquals("Cannot set saving account when current account is inactive", exception.getMessage());
    }

    @Test
    void gettingAndSettingId() {
        user.setId(1);
        Assertions.assertEquals(1, user.getId());
    }

    @Test
    void gettingAndSettingFirstName() {
        user.setFirstName("John");
        Assertions.assertEquals("John", user.getFirstName());
    }

    @Test
    void gettingAndSettingPhoneNumber() {
        user.setPhoneNumber("0612345678");
        Assertions.assertEquals("0612345678", user.getPhoneNumber());
    }

    @Test
    void gettingAndSettingDateOfBirth() {
        user.setDateOfBirth(LocalDate.of(1999, 1, 1));
        Assertions.assertEquals(LocalDate.of(1999, 1, 1), user.getDateOfBirth());
    }

    @Test
    void gettingAndSettingActive() {
        user.setActive(true);
        Assertions.assertTrue(user.isActive());
    }

    @Test
    void gettingAndSettingUserLimits() {
        Limits limits = new Limits();
        user.setLimits(limits);
        Assertions.assertEquals(limits, user.getLimits());
    }

    @Test
    void gettingAndSettingUsername() {
        user.setUsername("JohnDoe");
        Assertions.assertEquals("JohnDoe", user.getUsername());
    }

    @Test
    void settingLastNameEmptyThrowsException() {
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            user.setLastName("");
        });

        Assertions.assertEquals("Last name cannot be empty", exception.getMessage());
    }

    @Test
    void getSetLastName() {
        user.setLastName("Doe");
        Assertions.assertEquals("Doe", user.getLastName());
    }

    @Test
    void compareUserToAnotherUserReturnsFalse() {
        User user2 = new User();
        user2.setId(2);
        user2.setFirstName("Jane");
        user2.setLastName("Doe");

        Assertions.assertNotEquals(user, user2);
    }

    @Test
    void compareUserToItselfReturnsTrue() {
        Assertions.assertEquals(user, user);
    }

    @Test
    void compareUserToOtherObjectReturnsFalse() {
        Assertions.assertNotEquals(user, new Object());
    }

    @Test
    void getHashCode() {
        Assertions.assertEquals(31, user.hashCode());
    }

    @Test
    void userMustBeAtleast18YearsOld() {
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            user.setDateOfBirth(LocalDate.now().minusYears(17));
        });

        Assertions.assertEquals("User must be 18 years or older", exception.getMessage());
    }
}
