package nl.inholland.bank.services;

import nl.inholland.bank.configuration.ApiTestConfiguration;
import nl.inholland.bank.models.*;
import nl.inholland.bank.models.dtos.TransactionDTO.TransactionRequest;
import nl.inholland.bank.models.dtos.TransactionDTO.TransactionSearchRequest;
import nl.inholland.bank.models.dtos.TransactionDTO.WithdrawDepositRequest;
import nl.inholland.bank.models.exceptions.*;
import nl.inholland.bank.repositories.TransactionRepository;
import nl.inholland.bank.repositories.UserRepository;
import nl.inholland.bank.utils.JwtTokenProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.naming.InsufficientResourcesException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.sasl.AuthenticationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
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

    @Autowired
    private JwtTokenProvider mockJwtTokenProvider;

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
        user.setId(1);
        user2.setId(2);
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
        TransactionRequest request = new TransactionRequest("NL10INHO6628932884", "NL89INHO9277178029", 100.0, "description");
        when(userRepository.findUserByUsername(anyString())).thenReturn(java.util.Optional.of(new User()));
        when(accountService.getAccountByIBAN("NL10INHO6628932884")).thenReturn(null);

        assertThrows(AccountNotFoundException.class, () -> transactionService.processTransaction(request));
        verify(accountService, times(1)).getAccountByIBAN("NL10INHO6628932884");
    }

    @Test
    void processTransaction_UserNotTheOwnerOfAccountException() throws AccountNotFoundException {
        User user = new User();
        Account accountSender = new Account();
        Account accountReceiver = new Account();
        TransactionRequest request = new TransactionRequest("NL10INHO6628932884", "NL89INHO9277178029", 100.0, "description");

        Mockito.when(userRepository.findUserByUsername(Mockito.anyString())).thenReturn(Optional.of(user));

        Mockito.when(accountService.getAccountByIBAN(Mockito.anyString())).thenReturn(accountSender, accountReceiver);

        Assertions.assertThrows(UserNotTheOwnerOfAccountException.class, () -> {
            transactionService.checkUserAuthorization(user, accountReceiver);
        });
    }

    @Test
    void processTransaction_SenderAccountInactive() throws AccountNotFoundException, InsufficientResourcesException,
            UserNotTheOwnerOfAccountException, javax.naming.AuthenticationException {
        // Create a mock User
        User user = this.user;
        when(userService.getBearerUsername()).thenReturn("username");
        when(userRepository.findUserByUsername("username")).thenReturn(java.util.Optional.of(user));

        // Set to inactive
        Account accountSender = this.currentAccount;
        accountSender.setActive(false);

        Account accountReceiver = this.currentAccount2;

        // Create a TransactionRequest
        TransactionRequest request = new TransactionRequest("NL10INHO6628932884", "NL89INHO9277178029", 100.0, "description");

        Mockito.when(accountService.getAccountByIBAN(request.sender_iban())).thenReturn(accountSender);
        Mockito.when(accountService.getAccountByIBAN(request.receiver_iban())).thenReturn(accountReceiver);
        Mockito.when(userService.getBearerUserRole()).thenReturn(Role.EMPLOYEE);

        // Assert that an InactiveAccountException is thrown
        assertThrows(InactiveAccountException.class, () -> transactionService.processTransaction(request));
    }

    @Test
    void processTransaction_ReceiverAccountInactive() throws AccountNotFoundException, InsufficientResourcesException,
            UserNotTheOwnerOfAccountException, javax.naming.AuthenticationException {
        // Create a mock User
        User user = this.user;
        when(userService.getBearerUsername()).thenReturn("username");
        when(userRepository.findUserByUsername("username")).thenReturn(java.util.Optional.of(user));

        Account accountSender = this.currentAccount;

        // Set to inactive
        Account accountReceiver = this.currentAccount2;
        accountReceiver.setActive(false);

        // Create a TransactionRequest
        TransactionRequest request = new TransactionRequest("NL10INHO6628932884", "NL89INHO9277178029", 100.0, "description");

        Mockito.when(accountService.getAccountByIBAN(request.sender_iban())).thenReturn(accountSender);
        Mockito.when(accountService.getAccountByIBAN(request.receiver_iban())).thenReturn(accountReceiver);
        Mockito.when(userService.getBearerUserRole()).thenReturn(Role.EMPLOYEE);

        // Assert that an InactiveAccountException is thrown
        assertThrows(InactiveAccountException.class, () -> transactionService.processTransaction(request));
    }

    @Test
    void processTransaction_SameAccount() throws AccountNotFoundException, InsufficientResourcesException,
            UserNotTheOwnerOfAccountException, javax.naming.AuthenticationException {
        // Create a mock User
        User user = this.user;
        when(userService.getBearerUsername()).thenReturn("username");
        when(userRepository.findUserByUsername("username")).thenReturn(java.util.Optional.of(user));

        Account accountSender = this.currentAccount;

        // Set to inactive
        Account accountReceiver = this.currentAccount;

        // Create a TransactionRequest
        TransactionRequest request = new TransactionRequest("NL10INHO6628932884", "NL10INHO6628932884", 100.0, "description");

        Mockito.when(accountService.getAccountByIBAN(request.sender_iban())).thenReturn(accountSender);
        Mockito.when(accountService.getAccountByIBAN(request.receiver_iban())).thenReturn(accountReceiver);
        Mockito.when(userService.getBearerUserRole()).thenReturn(Role.EMPLOYEE);

        // Assert that an InactiveAccountException is thrown
        assertThrows(SameAccountTransferException.class, () -> transactionService.processTransaction(request));
    }

    @Test
    void processTransaction_checkSavingAccountOwnership() throws AccountNotFoundException, InsufficientResourcesException,
            UserNotTheOwnerOfAccountException, javax.naming.AuthenticationException {
        // Create a mock User
        User user = this.user;
        when(userService.getBearerUsername()).thenReturn("username");
        when(userRepository.findUserByUsername("username")).thenReturn(java.util.Optional.of(user));

        Account accountSender = this.currentAccount;
        accountSender.setUser(user);

        // Set to inactive
        Account accountReceiver = this.savingAccount;
        accountReceiver.setUser(user2);

        // Create a TransactionRequest
        TransactionRequest request = new TransactionRequest("NL10INHO6628932884", "NL10INHO6628923423", 100.0, "description");

        Mockito.when(accountService.getAccountByIBAN(request.sender_iban())).thenReturn(accountSender);
        Mockito.when(accountService.getAccountByIBAN(request.receiver_iban())).thenReturn(accountReceiver);
        Mockito.when(userService.getBearerUserRole()).thenReturn(Role.CUSTOMER);

        // Assert that an InactiveAccountException is thrown
        assertThrows(UserNotTheOwnerOfAccountException.class, () -> transactionService.processTransaction(request));
    }

    @Test
    void processTransaction_checkUserLimitsExceedTransactionLimit() throws AccountNotFoundException, InsufficientResourcesException,
            UserNotTheOwnerOfAccountException, javax.naming.AuthenticationException {
        // Create a mock User
        User user = this.user;
        when(userService.getBearerUsername()).thenReturn("username");
        when(userRepository.findUserByUsername("username")).thenReturn(java.util.Optional.of(user));

        Account accountSender = this.currentAccount;
        accountSender.setUser(user);
        accountSender.setBalance(2000);

        // Set to inactive
        Account accountReceiver = this.currentAccount2;
        accountReceiver.setUser(user2);

        Limits limits = new Limits();
        limits.setTransactionLimit(500);
        limits.setDailyTransactionLimit(1000);

        // Create a TransactionRequest
        TransactionRequest request = new TransactionRequest("NL10INHO6628932884", "NL10INHO6628923423", 600.0, "description");

        Mockito.when(accountService.getAccountByIBAN(request.sender_iban())).thenReturn(accountSender);
        Mockito.when(accountService.getAccountByIBAN(request.receiver_iban())).thenReturn(accountReceiver);
        Mockito.when(userService.getBearerUserRole()).thenReturn(Role.CUSTOMER);
        Mockito.when(userLimitsService.getUserLimits(accountSender.getUser().getId())).thenReturn(limits);

        // Assert that an InactiveAccountException is thrown
        assertThrows(TransactionLimitException.class, () -> transactionService.processTransaction(request));
    }

    @Test
    void processTransaction_checkUserLimitsExceedDailyTransactionLimit() throws AccountNotFoundException, InsufficientResourcesException,
            UserNotTheOwnerOfAccountException, javax.naming.AuthenticationException {
        // Create a mock User
        User user = this.user;
        when(userService.getBearerUsername()).thenReturn("username");
        when(userRepository.findUserByUsername("username")).thenReturn(java.util.Optional.of(user));

        Account accountSender = this.currentAccount;
        accountSender.setUser(user);
        accountSender.setBalance(2000);

        // Set to inactive
        Account accountReceiver = this.currentAccount2;
        accountReceiver.setUser(user2);

        Limits limits = new Limits();
        limits.setTransactionLimit(1500);
        limits.setDailyTransactionLimit(1000);

        // Create a TransactionRequest
        TransactionRequest request = new TransactionRequest("NL10INHO6628932884", "NL10INHO6628923423", 1100.0, "description");

        Mockito.when(accountService.getAccountByIBAN(request.sender_iban())).thenReturn(accountSender);
        Mockito.when(accountService.getAccountByIBAN(request.receiver_iban())).thenReturn(accountReceiver);
        Mockito.when(userService.getBearerUserRole()).thenReturn(Role.CUSTOMER);
        Mockito.when(userLimitsService.getUserLimits(accountSender.getUser().getId())).thenReturn(limits);

        // Assert that an InactiveAccountException is thrown
        assertThrows(DailyTransactionLimitException.class, () -> transactionService.processTransaction(request));
    }

    @Test
    void processTransaction_checkUserLimitsInsufficientFunds() throws AccountNotFoundException, InsufficientResourcesException,
            UserNotTheOwnerOfAccountException, javax.naming.AuthenticationException {
        // Create a mock User
        User user = this.user;
        when(userService.getBearerUsername()).thenReturn("username");
        when(userRepository.findUserByUsername("username")).thenReturn(java.util.Optional.of(user));

        Account accountSender = this.currentAccount;
        accountSender.setUser(user);
        accountSender.setBalance(100);
        accountSender.setAbsoluteLimit(0);

        // Set to inactive
        Account accountReceiver = this.currentAccount2;
        accountReceiver.setUser(user2);

        Limits limits = new Limits();
        limits.setTransactionLimit(500);
        limits.setDailyTransactionLimit(1000);
        limits.setRemainingDailyTransactionLimit(450);

        // Create a TransactionRequest
        TransactionRequest request = new TransactionRequest("NL10INHO6628932884", "NL10INHO6628923423", 200.0, "description");

        Mockito.when(accountService.getAccountByIBAN(request.sender_iban())).thenReturn(accountSender);
        Mockito.when(accountService.getAccountByIBAN(request.receiver_iban())).thenReturn(accountReceiver);
        Mockito.when(userService.getBearerUserRole()).thenReturn(Role.CUSTOMER);
        Mockito.when(userLimitsService.getUserLimits(accountSender.getUser().getId())).thenReturn(limits);

        // Assert that an InactiveAccountException is thrown
        assertThrows(InsufficientFundsException.class, () -> transactionService.processTransaction(request));
    }

    @Test
    void processTransaction_transferMoney() throws AccountNotFoundException, InsufficientResourcesException,
            UserNotTheOwnerOfAccountException, javax.naming.AuthenticationException {
        // Create a mock User
        User user = this.user;
        when(userService.getBearerUsername()).thenReturn("username");
        when(userRepository.findUserByUsername("username")).thenReturn(java.util.Optional.of(user));

        Account accountSender = this.currentAccount;
        accountSender.setUser(user);
        accountSender.setBalance(100);
        accountSender.setAbsoluteLimit(0);

        // Set to inactive
        Account accountReceiver = this.currentAccount2;
        accountReceiver.setUser(user2);

        Limits limits = new Limits();
        limits.setTransactionLimit(500);
        limits.setDailyTransactionLimit(1000);
        limits.setRemainingDailyTransactionLimit(450);

        // Create a TransactionRequest
        TransactionRequest request = new TransactionRequest("NL10INHO6628932884", "NL10INHO6628923423", 100.0, "description");

        Mockito.when(accountService.getAccountByIBAN(request.sender_iban())).thenReturn(accountSender);
        Mockito.when(accountService.getAccountByIBAN(request.receiver_iban())).thenReturn(accountReceiver);
        Mockito.when(userService.getBearerUserRole()).thenReturn(Role.CUSTOMER);
        Mockito.when(userLimitsService.getUserLimits(accountSender.getUser().getId())).thenReturn(limits);

        // Assert that an InactiveAccountException is thrown
        assertDoesNotThrow(() -> transactionService.processTransaction(request));
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
        user.setRole(Role.CUSTOMER); // Set the user role to USER

        Account account = currentAccount2;
        account.setUser(user2); // Different user object

        boolean isAuthorized = transactionService.isUserAuthorizedForTransaction(user, account);

        Assertions.assertFalse(isAuthorized);
    }

    @Test
    void isUserAuthorizedForTransaction_ShouldReturnTrueForUserRoleIsEmployee() {
        User user = this.user;
        user.setRole(Role.CUSTOMER); // Set the user role to USER

        Account account = currentAccount2;
        account.setUser(user2); // Different user object
        Mockito.when(userService.getBearerUserRole()).thenReturn(Role.EMPLOYEE);

        Assertions.assertTrue(transactionService.isUserAuthorizedForTransaction(user, account));
    }

    @Test
    void isUserAuthorizedForTransaction_ShouldReturnFalseForWrongUser() {
        User user = this.user;
        user.setRole(Role.CUSTOMER); // Set the user role to USER

        Account account = currentAccount2;
        account.setUser(user2); // Different user object
        Mockito.when(userService.getBearerUserRole()).thenReturn(Role.CUSTOMER);

        Assertions.assertFalse(transactionService.isUserAuthorizedForTransaction(user, account));
    }

    @Test
    void checkAccountStatus_ShouldThrowExceptionWhenAccountIsInactive() {
        Account account = savingAccount;
        String accountType = "Saving";

        account.setActive(false); // Set the account as inactive

        Assertions.assertThrows(InactiveAccountException.class, () -> {
            transactionService.checkAccountStatus(account, accountType);
        });
    }

    @Test
    void checkAccountStatus_ShouldNotThrowExceptionWhenAccountIsActive() {
        Account account = currentAccount;
        String accountType = "Current";

        account.setActive(true); // Set the account as active

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

        Assertions.assertThrows(TransactionLimitException.class, () -> {
            transactionService.checkUserLimits(accountSender, amount);
        });

        // Verify
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
        Account account = new Account();
        account.setBalance(100.0);
        double amount = 50.0;
        boolean isDeposit = true;

        transactionService.updateAccountBalance(account, amount, isDeposit);

        double expectedBalance = 150.0;
        Assertions.assertEquals(expectedBalance, account.getBalance());
        Mockito.verify(accountService, Mockito.times(1)).updateAccount(account);
    }

    @Test
    void updateAccountBalance_WhenIsNotDeposit_ShouldDecreaseBalance() {
        Account account = new Account();
        account.setBalance(100.0);
        double amount = 50.0;
        boolean isDeposit = false;

        transactionService.updateAccountBalance(account, amount, isDeposit);

        double expectedBalance = 50.0;
        Assertions.assertEquals(expectedBalance, account.getBalance());
        Mockito.verify(accountService, Mockito.times(1)).updateAccount(account);
    }

    @Test
    void mapTransactionTypeToString_WhenDeposit_ReturnsDeposit() {
        String transactionType = "deposit";

        TransactionType result = transactionService.mapTransactionTypeToString(transactionType);

        Assertions.assertEquals(TransactionType.DEPOSIT, result);
    }

    @Test
    void mapTransactionTypeToString_WhenWithdrawal_ReturnsWithdrawal() {
        String transactionType = "withdrawal";

        TransactionType result = transactionService.mapTransactionTypeToString(transactionType);

        Assertions.assertEquals(TransactionType.WITHDRAWAL, result);
    }

    @Test
    void mapTransactionTypeToString_WhenUnknownTransaction_ReturnsTransaction() {
        String transactionType = "unknown";

        TransactionType result = transactionService.mapTransactionTypeToString(transactionType);

        Assertions.assertEquals(TransactionType.TRANSACTION, result);
    }

    @Test
    void testTransferMoney() {
        when(transactionRepository.save(any(Transaction.class))).thenReturn(mock(Transaction.class));

        Transaction result = transactionService.transferMoney(
                user, currentAccount, currentAccount2, CurrencyType.EURO, 100.0, "Transfer");

        assertNotNull(result);
        assertEquals(TransactionType.TRANSACTION, result.getTransactionType());
    }

    @Test
    void testGetTransactions_UnauthorizedAccess_ThrowsAuthenticationException() {
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

        Mockito.when(userService.getBearerUsername()).thenReturn("billy");
        Mockito.when(userRepository.findUserByUsername("billy")).thenReturn(Optional.of(user));
        Mockito.when(accountService.getAccountByIBAN("NL10INHO6628932884")).thenReturn(accountReceiver);
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
