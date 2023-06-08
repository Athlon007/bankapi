package nl.inholland.bank.models;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTests {
    @Test
    void testConstructor() {
        // Create test data
        User user = new User();
        Account accountSender = new Account();
        Account accountReceiver = new Account();
        double amount = 100.0;
        CurrencyType currencyType = CurrencyType.EURO;
        TransactionType transactionType = TransactionType.DEPOSIT;

        // Create a transaction using the constructor
        Transaction transaction = new Transaction(user, accountSender, accountReceiver, amount, currencyType, transactionType);

        // Verify the properties of the created transaction
        assertNotNull(transaction);
        assertEquals(user, transaction.getUser());
        assertEquals(accountSender, transaction.getAccountSender());
        assertEquals(accountReceiver, transaction.getAccountReceiver());
        assertEquals(amount, transaction.getAmount());
        assertEquals(currencyType, transaction.getCurrencyType());
        assertEquals(transactionType, transaction.getTransactionType());
        assertNotNull(transaction.getTimestamp());
    }

    @Test
    void testSetAmount_ValidAmount_Success() {
        Transaction transaction = new Transaction();
        transaction.setAmount(100.0);
        assertEquals(100.0, transaction.getAmount());
    }

    @Test
    void testSetAmount_NegativeAmount_ThrowsException() {
        Transaction transaction = new Transaction();
        assertThrows(IllegalArgumentException.class, () -> transaction.setAmount(-50.0));
    }

    @Test
    void testSetCurrencyType_ValidCurrencyType_Success() {
        Transaction transaction = new Transaction();
        transaction.setCurrencyType(CurrencyType.EURO);
        assertEquals(CurrencyType.EURO, transaction.getCurrencyType());
    }

    @Test
    void testSetCurrencyType_NullCurrencyType_ThrowsException() {
        Transaction transaction = new Transaction();
        assertThrows(IllegalArgumentException.class, () -> transaction.setCurrencyType(null));
    }

    @Test
    void testSetTransactionType_ValidTransactionType_Success() {
        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.DEPOSIT);
        assertEquals(TransactionType.DEPOSIT, transaction.getTransactionType());
    }

    @Test
    void testSetTransactionType_NullTransactionType_ThrowsException() {
        Transaction transaction = new Transaction();
        assertThrows(IllegalArgumentException.class, () -> transaction.setTransactionType(null));
    }

    @Test
    void testSetAccountSender() {
        // Create test data
        Account accountSender = new Account();
        Transaction transaction = new Transaction();

        // Set the account sender
        transaction.setAccountSender(accountSender);

        // Verify the account sender is set correctly
        assertEquals(accountSender, transaction.getAccountSender());
    }

    @Test
    void testSetAccountReceiver() {
        // Create test data
        Account accountReceiver = new Account();
        Transaction transaction = new Transaction();

        // Set the account receiver
        transaction.setAccountReceiver(accountReceiver);

        // Verify the account receiver is set correctly
        assertEquals(accountReceiver, transaction.getAccountReceiver());
    }

    @Test
    void testSetUser_ValidUser_Success() {
        User user = new User();
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        assertEquals(user, transaction.getUser());
    }

    @Test
    void testSetUser_NullUser_ThrowsException() {
        Transaction transaction = new Transaction();
        assertThrows(IllegalArgumentException.class, () -> transaction.setUser(null));
    }

    @Test
    void testSetTimestamp_ValidTimestamp_Success() {
        LocalDateTime timestamp = LocalDateTime.now();
        Transaction transaction = new Transaction();
        transaction.setTimestamp(timestamp);
        assertEquals(timestamp, transaction.getTimestamp());
    }

    @Test
    void testSetTimestamp_NullTimestamp_ThrowsException() {
        Transaction transaction = new Transaction();
        assertThrows(IllegalArgumentException.class, () -> transaction.setTimestamp(null));
    }

    @Test
    void testSetTimestamp_FutureTimestamp_ThrowsException() {
        LocalDateTime futureTimestamp = LocalDateTime.now().plusDays(1);
        Transaction transaction = new Transaction();
        assertThrows(IllegalArgumentException.class, () -> transaction.setTimestamp(futureTimestamp));
    }
}
