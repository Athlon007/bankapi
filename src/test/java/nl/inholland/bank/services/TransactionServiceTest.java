package nl.inholland.bank.services;

import io.cucumber.java.en.When;
import nl.inholland.bank.configuration.ApiTestConfiguration;
import nl.inholland.bank.models.*;
import nl.inholland.bank.models.dtos.TransactionDTO.TransactionRequest;
import nl.inholland.bank.models.dtos.TransactionDTO.TransactionSearchRequest;
import nl.inholland.bank.models.dtos.TransactionDTO.WithdrawDepositRequest;
import nl.inholland.bank.models.exceptions.*;
import nl.inholland.bank.repositories.AccountRepository;
import nl.inholland.bank.repositories.TransactionRepository;
import nl.inholland.bank.repositories.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.naming.InsufficientResourcesException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.sasl.AuthenticationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import(ApiTestConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionServiceTest {
    private TransactionService transactionService;

    @MockBean
    private TransactionRepository transactionRepository;

    @MockBean
    private UserRepository userRepository;


    @MockBean
    private UserService userService;

    @MockBean
    private AccountService accountService;

    @MockBean
    private UserLimitsService userLimitsService;

    private User user, user2;
    private Account currentAccount, currentAccount2, savingAccount, savingAccount2;

    @BeforeEach
    public void setup() {
        transactionService = new TransactionService(transactionRepository, userRepository, userService,
                accountService, userLimitsService);

        user = new User(
                "Billy",
                "Bob",
                "billy@example.com",
                "570372562",
                "0612345678",
                LocalDate.of(1990, 1, 1),
                "billy",
                "P@ssw0rd",
                Role.CUSTOMER
        );
        currentAccount = new Account(
                user,
                750.0,
                CurrencyType.EURO,
                "NL10INHO6628932884",
                AccountType.CURRENT,
                0.0
        );
        savingAccount = new Account(
                user,
                50.0,
                CurrencyType.EURO,
                "NL19INHO2943276296",
                AccountType.SAVING,
                0.0
        );
        user2 = new User(
                "Berta",
                "Bob",
                "berta@example.com",
                "333550808",
                "0612345333",
                LocalDate.of(1992, 1, 1),
                "berta",
                "P@ssw0rd",
                Role.CUSTOMER
        );
        currentAccount2 = new Account(
                user2,
                345.0,
                CurrencyType.EURO,
                "NL89INHO9277178029",
                AccountType.CURRENT,
                -100.0
        );
        savingAccount2 = new Account(
                user2,
                0.0,
                CurrencyType.EURO,
                "NL17INHO7534731756",
                AccountType.SAVING,
                0.0
        );
        userService.assignAccountToUser(user, currentAccount);
        userService.assignAccountToUser(user, savingAccount);
        userService.assignAccountToUser(user2, currentAccount2);
        userService.assignAccountToUser(user2, savingAccount2);
    }

    @Test
    void createTransactionTest() {
        User user = this.user;
        Account accountSender = currentAccount;
        Account accountReceiver = currentAccount2;
        CurrencyType currencyType = CurrencyType.EURO;
        double amount = 100.0;
        String description = "Successfully transferred 100 EURO";
        TransactionType transactionType = TransactionType.TRANSACTION;

        // Execute the method
        Transaction transaction = transactionService.createTransaction(user, accountSender, accountReceiver,
                currencyType, amount, description, transactionType);

        // Assertions
        assertEquals(user, transaction.getUser());
        assertEquals(accountSender, transaction.getAccountSender());
        assertEquals(accountReceiver, transaction.getAccountReceiver());
        assertEquals(currencyType, transaction.getCurrencyType());
        assertEquals(amount, transaction.getAmount());
        assertEquals(transactionType, transaction.getTransactionType());
        assertEquals(LocalDateTime.now().getYear(), transaction.getTimestamp().getYear());
        assertEquals(LocalDateTime.now().getMonth(), transaction.getTimestamp().getMonth());
        assertEquals(LocalDateTime.now().getDayOfMonth(), transaction.getTimestamp().getDayOfMonth());
        assertEquals(LocalDateTime.now().getHour(), transaction.getTimestamp().getHour());
        assertEquals(LocalDateTime.now().getMinute(), transaction.getTimestamp().getMinute());
        assertEquals(LocalDateTime.now().getSecond(), transaction.getTimestamp().getSecond());
    }

    @Test
    void isTransactionAuthorizedForUserAccountTest() {
        // Test case setup
        User user = this.user;
        Account account = currentAccount;
        account.setUser(user);

        // Test when user is authorized for the account
        boolean isAuthorized = transactionService.isUserAuthorizedToAccessAccount(user, account);
        assertTrue(isAuthorized);

        // Test when user is not authorized for the account
        User otherUser = new User();
        boolean isNotAuthorized = transactionService.isUserAuthorizedToAccessAccount(otherUser, account);
        assertFalse(isNotAuthorized);
    }


    @Test
    void processTransaction_AccountNotFoundException() throws AccountNotFoundException {
        // Arrange
        TransactionRequest request = new TransactionRequest("sender_iban", "receiver_iban", 100.0, "description");
        when(userRepository.findUserByUsername(anyString())).thenReturn(java.util.Optional.of(new User()));
        when(accountService.getAccountByIBAN("sender_iban")).thenReturn(null);

        assertThrows(AccountNotFoundException.class, () -> transactionService.processTransaction(request));
        verify(accountService, times(1)).getAccountByIBAN("sender_iban");
    }

    @Test
    void processTransaction_UserNotTheOwnerOfAccountException() throws AccountNotFoundException {
        // Arrange
        TransactionRequest request = new TransactionRequest(currentAccount.getIBAN(), currentAccount2.getIBAN(), 100.0, "description");
        User user = this.user2;
        Account accountSender = this.currentAccount;
        Account accountReceiver = this.currentAccount2;
        Mockito.when(userRepository.findUserByUsername(anyString())).thenReturn(java.util.Optional.of(user));
        Mockito.when(accountService.getAccountByIBAN(request.sender_iban())).thenReturn(accountSender);
        Mockito.when(accountService.getAccountByIBAN(request.receiver_iban())).thenReturn(accountReceiver);

        assertThrows(UserNotTheOwnerOfAccountException.class, () -> transactionService.processTransaction(request));
    }

    @Test
    void checkAccountStatus_AccountIsInactive_InactiveAccountExceptionThrown() {
        // Arrange
        Account account = new Account();
        account.setActive(false);
        String accountType = "current";

        // Act and Assert
        InactiveAccountException exception = assertThrows(InactiveAccountException.class, () -> transactionService.checkAccountStatus(account, accountType));
        assertEquals("The current account is currently inactive and can't transfer money.", exception.getMessage());
    }

    @Test
    void isUserAuthorizedForTransaction_ShouldReturnFalseForMismatchedUserAndUserRoleIsUser() {
        User user = this.user;
        Account account = currentAccount;
        account.setUser(user2); // Different user object

        user.setRole(Role.CUSTOMER); // Set the user role to USER

        // Act
        boolean isAuthorized = transactionService.isUserAuthorizedForTransaction(user, account);

        // Assert
        Assertions.assertFalse(isAuthorized);
    }

    @Test
    void checkAccountStatus_ShouldThrowExceptionWhenAccountIsInactive() {
        Account account = savingAccount;
        String accountType = "Saving";

        account.setActive(false); // Set the account as inactive

        // Act and Assert
        Assertions.assertThrows(InactiveAccountException.class, () -> {
            transactionService.checkAccountStatus(account, accountType);
        });
    }

    @Test
    void checkAccountStatus_ShouldNotThrowExceptionWhenAccountIsActive() {
        Account account = currentAccount;
        String accountType = "Current";

        account.setActive(true); // Set the account as active

        // Act and Assert
        Assertions.assertDoesNotThrow(() -> {
            transactionService.checkAccountStatus(account, accountType);
        });
    }

    @Test
    void checkSameAccount_ShouldThrowExceptionWhenAccountsAreSame() {
        Account accountSender = this.currentAccount;
        Account accountReceiver = this.currentAccount;

        accountSender.setIBAN(accountSender.getIBAN());
        accountReceiver.setIBAN(accountReceiver.getIBAN()); // Set the same IBAN for both accounts

        // Act and Assert
        Assertions.assertThrows(SameAccountTransferException.class, () -> {
            transactionService.checkSameAccount(accountSender, accountReceiver);
        });
    }

    @Test
    void checkSameAccount_ShouldNotThrowExceptionWhenAccountsAreDifferent() {
        Account accountSender = this.currentAccount;
        Account accountReceiver = this.currentAccount2;

        accountSender.setIBAN(accountSender.getIBAN());
        accountReceiver.setIBAN(accountReceiver.getIBAN());

        // Act and Assert
        Assertions.assertDoesNotThrow(() -> {
            transactionService.checkSameAccount(accountSender, accountReceiver);
        });
    }

    @Test
    void checkSavingAccountOwnership_ShouldThrowExceptionWhenUserNotOwnerOfSavingAccounts() {
        User user = this.user;
        Account accountSender = this.currentAccount;
        Account accountReceiver = this.currentAccount2;

        accountSender.setType(AccountType.SAVING);
        accountReceiver.setType(AccountType.SAVING);
        accountSender.setUser(new User()); // Set up necessary dependencies
        accountReceiver.setUser(user);

        // Act and Assert
        Assertions.assertThrows(UserNotTheOwnerOfAccountException.class, () -> {
            transactionService.checkSavingAccountOwnership(user, accountSender, accountReceiver);
        });

        Assertions.assertThrows(UserNotTheOwnerOfAccountException.class, () -> {
            transactionService.checkSavingAccountOwnership(user, accountReceiver, accountSender);
        });
    }

    @Test
    void checkSavingAccountOwnership_ShouldNotThrowExceptionWhenUserOwnerOfSavingAccounts() {
        User user = this.user;
        Account accountSender = this.currentAccount;
        Account accountReceiver = this.currentAccount2;

        accountSender.setType(AccountType.SAVING);
        accountReceiver.setType(AccountType.SAVING);
        accountSender.setUser(user);
        accountReceiver.setUser(user);

        // Act and Assert
        Assertions.assertDoesNotThrow(() -> {
            transactionService.checkSavingAccountOwnership(user, accountSender, accountReceiver);
        });

        Assertions.assertDoesNotThrow(() -> {
            transactionService.checkSavingAccountOwnership(user, accountReceiver, accountSender);
        });
    }

    @Test
    void checkUserLimitsTest_ExceedsTransactionLimit() throws Exception {
        Account accountSender = this.currentAccount;
        double amount = 550.0;

        Limits limits = new Limits();
        limits.setTransactionLimit(500.0);
        limits.setRemainingDailyTransactionLimit(1000.0);

        Mockito.when(userLimitsService.getUserLimits(this.user.getId())).thenReturn(limits);

        // Act and Assert
        Assertions.assertThrows(TransactionLimitException.class, () -> {
            transactionService.checkUserLimits(accountSender, amount);
        });

        // Verify that the getUserLimits method is called once with the correct argument
        Mockito.verify(userLimitsService, Mockito.times(1)).getUserLimits(this.user.getId());
    }

    @Test
    void checkUserLimitsTest_ExceedsDailyTransactionLimit() throws Exception {
        Account accountSender = this.currentAccount;
        double amount = 200.0;

        Limits limits = new Limits();
        limits.setTransactionLimit(500.0);
        limits.setRemainingDailyTransactionLimit(100.0);

        Mockito.when(userLimitsService.getUserLimits(this.user.getId())).thenReturn(limits);

        Assertions.assertThrows(DailyTransactionLimitException.class, () -> {
            transactionService.checkUserLimits(accountSender, amount);
        });

        Mockito.verify(userLimitsService, Mockito.times(1)).getUserLimits(this.user.getId());
    }

    @Test
    void checkUserLimitsTest_InsufficientFunds() throws Exception {
        Account accountSender = this.currentAccount;
        double amount = 800.0;

        Limits limits = new Limits();
        limits.setTransactionLimit(1000.0);
        limits.setRemainingDailyTransactionLimit(1000.0);

        Mockito.when(userLimitsService.getUserLimits(this.user.getId())).thenReturn(limits);

        Assertions.assertThrows(InsufficientFundsException.class, () -> {
            transactionService.checkUserLimits(accountSender, amount);
        });

        Mockito.verify(userLimitsService, Mockito.times(1)).getUserLimits(this.user.getId());
    }

    @Test
    void updateAccountBalance_WhenIsDeposit_ShouldIncreaseBalance() {
        // Arrange
        Account account = new Account();
        account.setBalance(100.0);
        double amount = 50.0;
        boolean isDeposit = true;

        // Act
        transactionService.updateAccountBalance(account, amount, isDeposit);

        // Assert
        double expectedBalance = 150.0;
        Assertions.assertEquals(expectedBalance, account.getBalance());
        Mockito.verify(accountService, Mockito.times(1)).updateAccount(account);
    }

    @Test
    void updateAccountBalance_WhenIsNotDeposit_ShouldDecreaseBalance() {
        // Arrange
        Account account = new Account();
        account.setBalance(100.0);
        double amount = 50.0;
        boolean isDeposit = false;

        // Act
        transactionService.updateAccountBalance(account, amount, isDeposit);

        // Assert
        double expectedBalance = 50.0;
        Assertions.assertEquals(expectedBalance, account.getBalance());
        Mockito.verify(accountService, Mockito.times(1)).updateAccount(account);
    }

    @Test
    void mapTransactionTypeToString_WhenDeposit_ReturnsDeposit() {
        // Arrange
        String transactionType = "deposit";

        // Act
        TransactionType result = transactionService.mapTransactionTypeToString(transactionType);

        // Assert
        Assertions.assertEquals(TransactionType.DEPOSIT, result);
    }

    @Test
    void mapTransactionTypeToString_WhenWithdrawal_ReturnsWithdrawal() {
        // Arrange
        String transactionType = "withdrawal";

        // Act
        TransactionType result = transactionService.mapTransactionTypeToString(transactionType);

        // Assert
        Assertions.assertEquals(TransactionType.WITHDRAWAL, result);
    }

    @Test
    void mapTransactionTypeToString_WhenUnknownTransaction_ReturnsTransaction() {
        // Arrange
        String transactionType = "unknown";

        // Act
        TransactionType result = transactionService.mapTransactionTypeToString(transactionType);

        // Assert
        Assertions.assertEquals(TransactionType.TRANSACTION, result);
    }

    @Test
    public void testTransferMoney() {
        when(transactionRepository.save(any(Transaction.class))).thenReturn(mock(Transaction.class));

        Transaction result = transactionService.transferMoney(
                user, currentAccount, currentAccount2, CurrencyType.EURO, 100.0, "Transfer");

        assertNotNull(result);
        assertEquals(TransactionType.TRANSACTION, result.getTransactionType());
    }

    @Test
    public void testGetTransactions_UnauthorizedAccess_ThrowsAuthenticationException() {
        when(userService.getBearerUserRole()).thenReturn(null);

        assertThrows(AuthenticationException.class, () -> {
            transactionService.getTransactions(
                    Optional.empty(), Optional.empty(), new TransactionSearchRequest(
                            Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                            Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                            Optional.empty(), Optional.empty()
                    )
            );
        });

        verify(userService, times(1)).getBearerUserRole();
        verifyNoInteractions(transactionRepository);
    }


    @Test
    void testDepositMoneySuccessfulDeposit() throws AccountNotFoundException, AuthenticationException, UserNotTheOwnerOfAccountException, InsufficientResourcesException {
        WithdrawDepositRequest depositRequest = new WithdrawDepositRequest("NL10INHO6628932884", 100, CurrencyType.EURO);
        User user = new User("Billy", "Bob", "billy@example.com", "570372562", "0612345678",
                LocalDate.of(1990, 1, 1), "billy", "P@ssw0rd", Role.CUSTOMER);
        Account accountReceiver = new Account(user, 750.0, CurrencyType.EURO, "NL10INHO6628932884",
                AccountType.CURRENT, 0.0);

        when(userService.getBearerUsername()).thenReturn("billy");
        when(userRepository.findUserByUsername("billy")).thenReturn(Optional.of(user));
        when(accountService.getAccountByIBAN("NL10INHO6628932884")).thenReturn(accountReceiver);

        transactionService.depositMoney(depositRequest);

        assertEquals(850, accountReceiver.getBalance());

        accountReceiver.setActive(false);
        Assertions.assertThrows(InactiveAccountException.class, () -> {
            transactionService.depositMoney(depositRequest);
        });

        accountReceiver.setActive(true);

        accountReceiver.setType(AccountType.SAVING);
        Assertions.assertThrows(OperationNotAllowedException.class, () -> {
            transactionService.depositMoney(depositRequest);
        });

        accountReceiver.setUser(null);
        Assertions.assertThrows(UserNotTheOwnerOfAccountException.class, () -> {
            transactionService.depositMoney(depositRequest);
        });
    }

    @Test
    void testWithdrawMoneySuccessfulWithdrawal() throws AccountNotFoundException, AuthenticationException, UserNotTheOwnerOfAccountException, InsufficientResourcesException, javax.naming.AuthenticationException {
        WithdrawDepositRequest depositRequest = new WithdrawDepositRequest("NL10INHO6628932884", 100, CurrencyType.EURO);
        User user = new User("Billy", "Bob", "billy@example.com", "570372562", "0612345678",
                LocalDate.of(1990, 1, 1), "billy", "P@ssw0rd", Role.CUSTOMER);
        Account accountReceiver = new Account(user, 750.0, CurrencyType.EURO, "NL10INHO6628932884",
                AccountType.CURRENT, 0.0);

        Limits limits = new Limits();
        limits.setTransactionLimit(500.0);
        limits.setRemainingDailyTransactionLimit(1000.0);


        when(userService.getBearerUsername()).thenReturn("billy");
        when(userRepository.findUserByUsername("billy")).thenReturn(Optional.of(user));
        when(accountService.getAccountByIBAN("NL10INHO6628932884")).thenReturn(accountReceiver);
        Mockito.when(userLimitsService.getUserLimits(this.user.getId())).thenReturn(limits);

        transactionService.withdrawMoney(depositRequest);

        assertEquals(650, accountReceiver.getBalance());

    }

    @Test
    void testGetTransactions_Authorized_ReturnsList() {
        when(userService.getBearerUserRole()).thenReturn(Role.CUSTOMER);
        Transaction transaction = new Transaction();
        when(transactionRepository.findTransactions(anyDouble(), anyDouble(), any(), any(), anyInt(), anyString(), anyString(), any(), any(), any(), any(), any())).thenReturn(new PageImpl<>(List.of(transaction)));

        assertDoesNotThrow(() -> {
            transactionService.getTransactions(
                    Optional.empty(), Optional.empty(), new TransactionSearchRequest(
                            Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                            Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                            Optional.empty(), Optional.empty()
                    )
            );
        });
    }

}
