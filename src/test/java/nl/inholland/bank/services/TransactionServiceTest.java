package nl.inholland.bank.services;

import nl.inholland.bank.configuration.ApiTestConfiguration;
import nl.inholland.bank.models.*;
import nl.inholland.bank.models.dtos.TransactionDTO.TransactionRequest;
import nl.inholland.bank.models.exceptions.UserNotTheOwnerOfAccountException;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.naming.AuthenticationException;
import javax.naming.InsufficientResourcesException;
import javax.security.auth.login.AccountNotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
                Role.USER
        );
        currentAccount = new Account(
                user,
                1000.0,
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
                Role.USER
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
        assertEquals(description, transaction.getDescription());
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
        boolean isAuthorized = transactionService.isTransactionAuthorizedForUserAccount(user, account);
        assertTrue(isAuthorized);

        // Test when user is not authorized for the account
        User otherUser = new User();
        boolean isNotAuthorized = transactionService.isTransactionAuthorizedForUserAccount(otherUser, account);
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
        Mockito.when(userRepository.findUserByUsername(anyString())).thenReturn(java.util.Optional.of(user));
        Mockito.when(accountService.getAccountByIBAN(request.sender_iban())).thenReturn(accountSender);
        Mockito.doThrow(UserNotTheOwnerOfAccountException.class)
                .when(transactionService).checkUserAuthorization(user, accountSender);

        assertThrows(UserNotTheOwnerOfAccountException.class, () -> transactionService.processTransaction(request));
        verify(transactionService, times(1)).checkUserAuthorization(user, accountSender);
    }

    @Test
    void processTransaction_SuccessfulTransaction() throws InsufficientResourcesException, AuthenticationException, AccountNotFoundException {
        // Arrange
        TransactionRequest request = new TransactionRequest(currentAccount.getIBAN(), currentAccount2.getIBAN(), 100.0, "description");
        User user = this.user;
        Account accountSender = this.currentAccount;
        Account accountReceiver = this.currentAccount2;
        Mockito.when(userRepository.findUserByUsername(anyString())).thenReturn(java.util.Optional.of(user));
        Mockito.when(accountService.getAccountByIBAN(currentAccount.getIBAN())).thenReturn(accountSender);
        Mockito.when(accountService.getAccountByIBAN(currentAccount2.getIBAN())).thenReturn(accountReceiver);
        Mockito.doNothing().when(transactionService).checkUserAuthorization(user, eq(accountSender));

        Transaction result = transactionService.processTransaction(request);
        assertNotNull(result);
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
}
