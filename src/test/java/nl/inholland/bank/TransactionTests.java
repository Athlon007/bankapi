package nl.inholland.bank;

import nl.inholland.bank.models.*;
import nl.inholland.bank.models.dtos.AccountDTO.AccountRequest;
import nl.inholland.bank.models.dtos.TransactionDTO.WithdrawDepositRequest;
import nl.inholland.bank.models.dtos.UserDTO.UserRequest;
import nl.inholland.bank.models.exceptions.UserNotTheOwnerOfAccountException;
import nl.inholland.bank.services.AccountService;
import nl.inholland.bank.services.TransactionService;
import nl.inholland.bank.services.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import javax.naming.AuthenticationException;
import javax.naming.InsufficientResourcesException;
import javax.security.auth.login.AccountNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;

@SpringBootTest
public class TransactionTests {
    private Transaction transaction;
    private Account account;

    @Mock
    private AccountService accountService;

    @Mock
    private UserService userService;

    @Mock
    private TransactionService transactionService;

    WithdrawDepositRequest withdrawDepositRequest;

    @BeforeEach
    public void setUp() throws AuthenticationException {
        transaction = new Transaction();
        account = new Account(new User(
                "John", "Doe", "mail@test.com", "111222333", "0612345678", LocalDate.of(1990, 1, 1), "username", "Password1!", Role.EMPLOYEE), 100, CurrencyType.EURO, IBANGenerator.generateIBAN().toString(), AccountType.CURRENT);
        transaction.setAmount(50);
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setCurrencyType(CurrencyType.EURO);
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setAccountReceiver(account);
        transaction.setAccountSender(account);
        transaction.setUser(account.getUser());

    }


    @Test
    void amountCannotBeNegative() {
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            transaction.setAmount(-100);
        });

        Assertions.assertEquals("Amount cannot be negative or zero", exception.getMessage());
    }

    @Test
    void transactionTypeCannotBeNull() {
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            transaction.setTransactionType(null);
        });

        Assertions.assertEquals("Transaction type cannot be null", exception.getMessage());
    }

    @Test
    void timestampCannotBeNull() {
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            transaction.setTimestamp(null);
        });

        Assertions.assertEquals("Timestamp cannot be null", exception.getMessage());
    }

    @Test
    void timestampCannotBeInTheFuture() {
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            transaction.setTimestamp(LocalDateTime.now().plusDays(1));
        });

        Assertions.assertEquals("Timestamp cannot be in the future", exception.getMessage());
    }

    @Test
    void accountReceiverCannotBeNull() {
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            transaction.setAccountReceiver(null);
        });

        org.junit.jupiter.api.Assertions.assertEquals("Account receiver cannot be null", exception.getMessage());
    }

    @Test
    void userCannotBeNull() {
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            transaction.setUser(null);
        });

        org.junit.jupiter.api.Assertions.assertEquals("User cannot be null", exception.getMessage());
    }

}
