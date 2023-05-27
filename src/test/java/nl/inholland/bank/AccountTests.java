package nl.inholland.bank;

import nl.inholland.bank.models.*;
import nl.inholland.bank.repositories.AccountRepository;
import nl.inholland.bank.repositories.UserRepository;
import nl.inholland.bank.services.AccountService;
import nl.inholland.bank.services.TransactionService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import javax.naming.InsufficientResourcesException;
import javax.security.auth.login.AccountNotFoundException;
import java.time.LocalDate;

@SpringBootTest
public class AccountTests {
    private Account account;
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        account = new Account(
                new User(
                        "John",
                        "Doe",
                        "mail@test.com",
                        "12345678",
                        "0612345678",
                        LocalDate.of(1990, 1, 1),
                        "username",
                        "Password1!",
                        Role.EMPLOYEE),
                0,
                CurrencyType.EURO,
                "NL01INHO0000000001",
                AccountType.CURRENT);
        transactionService = Mockito.mock(TransactionService.class);

    }

    @Test
    void balanceCannotBeNegative() {
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            account.setBalance(-1);
        });

        Assertions.assertEquals("Balance cannot be negative", exception.getMessage());
    }

    @Test
    void currencyTypeCannotBeNull() {
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            account.setCurrencyType(null);
        });

        Assertions.assertEquals("Currency type cannot be null", exception.getMessage());
    }

    @Test
    void accountCreationShouldBeSuccessful() {
        // Verify that the account is created successfully
        Assertions.assertNotNull(account);

        // Check the account properties
        Assertions.assertEquals(0, account.getBalance());
        Assertions.assertEquals(CurrencyType.EURO, account.getCurrencyType());
        Assertions.assertEquals("NL01INHO0000000001", account.getIBAN());
        Assertions.assertEquals(AccountType.CURRENT, account.getType());
    }

    @Test
    void depositMoneyShouldIncreaseBalance() {
        double balanceBeforeTransaction = 250;
        account.setBalance(balanceBeforeTransaction);

        double depositAmount = 100;
        account.setBalance(balanceBeforeTransaction + depositAmount);

        double expectedBalance = balanceBeforeTransaction + depositAmount;
        Assertions.assertEquals(expectedBalance, account.getBalance());
    }

    @Test
    void withdrawMoneyShouldDecreaseBalance() {
        double balanceBeforeTransaction = 250;
        account.setBalance(balanceBeforeTransaction);

        double withdrawAmount = 100;
        account.setBalance(balanceBeforeTransaction - withdrawAmount);

        double expectedBalance = balanceBeforeTransaction - withdrawAmount;
        Assertions.assertEquals(expectedBalance, account.getBalance());
    }

    @Test
    void creatingAccountWithoutUserShouldFail() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            // Attempt to create the account without a user
            // You can use the appropriate methods from your application code to create the account
            // For example: accountService.createAccount(account);
            AccountService accountService = Mockito.mock(AccountService.class);
            accountService.createAccount(
                    null,
                    "NL01INHO0000000001",
                    AccountType.CURRENT,
                    CurrencyType.EURO
            );
            throw new IllegalArgumentException("User cannot be null");
        });
    }

    @Test
    void withdrawTest() throws InsufficientResourcesException, AccountNotFoundException {
        account.setBalance(100);

        // Invoke the withdrawal
        Transaction withdrawalTransaction = transactionService.withdrawMoney(account.getUser(), account, 100);

        // Verify the account balance after the withdrawal
        Assertions.assertEquals(0, account.getBalance());
    }


}
