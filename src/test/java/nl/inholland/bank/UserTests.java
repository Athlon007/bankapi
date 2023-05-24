package nl.inholland.bank;

import nl.inholland.bank.models.Role;
import nl.inholland.bank.models.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest
public class UserTests {
    private User user;

    @BeforeEach
    void setUp() {
    user = new User(
            "John",
            "Doe",
            "mail@test.com",
            "12345678",
            "0612345678",
            LocalDate.of(1990, 1, 1),
            "username",
            "Password1!",
            Role.USER);
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
    void settingPasswordToNullThrowsIllegalArgumentException() {
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            user.setPassword(null);
        });

        Assertions.assertEquals("Password must be at least 8 characters long", exception.getMessage());
    }

    @Test
    void passwordCannotBeShorterThan8Characters() {
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            user.setPassword("1234567");
        });

        Assertions.assertEquals("Password must be at least 8 characters long", exception.getMessage());
    }

    @Test
    void passwordCannotHaveSingleCharacterTypes() {
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            user.setPassword("aaaaaaaa");
        });

        Assertions.assertEquals("Password cannot have repeating characters only", exception.getMessage());
    }

    @Test
    void passwordMustContainOneCapitalLetterOneNumberAndOneSpecialCharacter() {
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            user.setPassword("aaaaaaa1a");
        });
        Assertions.assertEquals("Password must contain at least one digit, one lowercase character, one uppercase character and one special character", exception.getMessage());

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            user.setPassword("Aaaaaaaaa");
        });

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            user.setPassword("aaaaaaaaa!");
        });

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            user.setPassword("Aaaaaaaaa!");
        });

        // Check valid password.
        user.setPassword("aA1!12345");
        Assertions.assertEquals("aA1!12345", user.getPassword());
    }
}
