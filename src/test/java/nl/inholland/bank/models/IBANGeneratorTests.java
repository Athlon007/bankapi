package nl.inholland.bank.models;

import org.iban4j.Iban;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IBANGeneratorTests {
    @Test
    void generateIBAN_ShouldReturnValidIBAN() {
        // Act
        Iban iban = IBANGenerator.generateIBAN();

        // Assert
        assertNotNull(iban);
        assertTrue(iban.toString().startsWith("NL"));
    }

    @Test
    void isValidIBAN_WithValidBankIBAN_ShouldReturnTrue() {
        // Arrange
        String bankIBAN = "NL01INHO0000000001";

        // Act
        boolean isValid = IBANGenerator.isValidIBAN(bankIBAN);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void isValidIBAN_WithValidNonBankIBAN_ShouldReturnTrue() {
        // Arrange
        String validIBAN = "NL91ABNA0417164300";

        // Act
        boolean isValid = IBANGenerator.isValidIBAN(validIBAN);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void isValidIBAN_WithInvalidIBAN_ShouldReturnFalse() {
        // Arrange
        String invalidIBAN = "NL00INHO0000000000";

        // Act
        boolean isValid = IBANGenerator.isValidIBAN(invalidIBAN);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void isValidIBAN_WithNullIBAN_ShouldReturnFalse() {
        // Act
        boolean isValid = IBANGenerator.isValidIBAN(null);

        // Assert
        assertFalse(isValid);
    }
}
