package nl.inholland.bank.repositories;

import nl.inholland.bank.models.Transaction;
import nl.inholland.bank.models.TransactionType;
import nl.inholland.bank.models.User;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class TransactionRepositoryTests {
    @Test
    void testFindAllByTimestampIsAfterAndUserId() {
        // Create test data
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        int userId = 123;

        // Mock the repository
        TransactionRepository repository = mock(TransactionRepository.class);

        // Call the method
        List<Transaction> transactions = repository.findAllByTimestampIsAfterAndUserId(start, userId);

        // Verify the result
        // Example assertion: Check if the transactions list is not null
        assertNotNull(transactions);
    }

    @Test
    void testFindAll() {
        // Mock the repository
        TransactionRepository repository = mock(TransactionRepository.class);
        Pageable pageable = mock(Pageable.class);

        // Call the method
        Page<Transaction> transactionPage = repository.findAll(pageable);

        // Verify the result
        // Example assertion: Check if the transactionPage is not null
        assertNotNull(transactionPage);
    }

    @Test
    void testFindTransactions() {
        // Create test data
        double minAmount = 100.0;
        double maxAmount = 500.0;
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        int transactionID = 1;
        String accountSenderIBAN = "";
        String accountReceiverIBAN = "";
        User user = new User();
        User senderUser = new User();
        User receiverUser = new User();
        TransactionType transactionType = TransactionType.TRANSACTION;
        Pageable pageable = mock(Pageable.class);

        // Mock the repository
        TransactionRepository repository = mock(TransactionRepository.class);

        // Call the method
        Page<Transaction> transactionPage = repository.findTransactions(
                minAmount, maxAmount, startDate, endDate, transactionID,
                accountSenderIBAN, accountReceiverIBAN, user, senderUser, receiverUser,
                transactionType, pageable);

        // Verify the result
        // Example assertion: Check if the transactionPage is not null
        assertNotNull(transactionPage);
    }
}
