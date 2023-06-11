package nl.inholland.bank.specifications;

import nl.inholland.bank.models.Transaction;
import nl.inholland.bank.models.TransactionType;
import nl.inholland.bank.models.User;
import nl.inholland.bank.models.specifications.TransactionSpecifications;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class TransactionSpecificationsTests {
    @Test
    void testWithAmountBetween() {
        // Create test data
        double minAmount = 100.0;
        double maxAmount = 500.0;

        // Call the method
        Specification<Transaction> specification = TransactionSpecifications.withAmountBetween(minAmount, maxAmount);

        // Verify the specification
        // Example assertion: Check if the specification is not null
        assertNotNull(specification);
    }

    @Test
    void testWithTimestampBetween() {
        // Create test data
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();

        // Call the method
        Specification<Transaction> specification = TransactionSpecifications.withTimestampBetween(startDate, endDate);

        // Verify the specification
        // Example assertion: Check if the specification is not null
        assertNotNull(specification);
    }

    @Test
    void testWithAccountSenderIBAN() {
        // Create test data
        String accountSenderIBAN = "NL49INHO2395649161";

        // Call the method
        Specification<Transaction> specification = TransactionSpecifications.withAccountSenderIBAN(accountSenderIBAN);

        // Verify the specification
        // Example assertion: Check if the specification is not null
        assertNotNull(specification);
    }

    @Test
    void testWithAccountReceiverIBAN() {
        // Create test data
        String accountReceiverIBAN = "NL50INHO8574952275";

        // Call the method
        Specification<Transaction> specification = TransactionSpecifications.withAccountReceiverIBAN(accountReceiverIBAN);

        // Verify the specification
        // Example assertion: Check if the specification is not null
        assertNotNull(specification);
    }

    @Test
    void testWithUserId() {
        // Create test data
        int userId = 123;

        // Call the method
        Specification<Transaction> specification = TransactionSpecifications.withUserId(userId);

        // Verify the specification
        // Example assertion: Check if the specification is not null
        assertNotNull(specification);
    }

    @Test
    void testWithSenderUser() {
        // Create test data
        User senderUser = new User();

        // Call the method
        Specification<Transaction> specification = TransactionSpecifications.withSenderUser(senderUser);

        // Verify the specification
        // Example assertion: Check if the specification is not null
        assertNotNull(specification);
    }

    @Test
    void testWithReceiverUser() {
        // Create test data
        User receiverUser = new User();

        // Call the method
        Specification<Transaction> specification = TransactionSpecifications.withReceiverUser(receiverUser);

        // Verify the specification
        // Example assertion: Check if the specification is not null
        assertNotNull(specification);
    }

    @Test
    void testWithTransactionID() {
        // Create test data
        int transactionID = 456;

        // Call the method
        Specification<Transaction> specification = TransactionSpecifications.withTransactionID(transactionID);

        // Verify the specification
        // Example assertion: Check if the specification is not null
        assertNotNull(specification);
    }

    @Test
    void testWithTransactionType() {
        // Create test data
        TransactionType transactionType = TransactionType.TRANSACTION;

        // Call the method
        Specification<Transaction> specification = TransactionSpecifications.withTransactionType(transactionType);

        // Verify the specification
        // Example assertion: Check if the specification is not null
        assertNotNull(specification);
    }
}
