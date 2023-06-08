package nl.inholland.bank.services;

import nl.inholland.bank.configuration.ApiTestConfiguration;
import nl.inholland.bank.models.*;
import nl.inholland.bank.models.dtos.AccountDTO.AccountAbsoluteLimitRequest;
import nl.inholland.bank.models.dtos.AccountDTO.AccountActiveRequest;
import nl.inholland.bank.models.dtos.AccountDTO.AccountRequest;
import nl.inholland.bank.repositories.AccountRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.security.auth.login.AccountNotFoundException;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@Import(ApiTestConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class AccountServiceTests {
    private AccountService accountService;

    @MockBean
    private AccountRepository accountRepository;

    @MockBean
    private UserService userService;

    private AccountRequest accountRequest;

    private Account account;

    private User user;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(accountRepository, userService);

        accountRequest = new AccountRequest(
                CurrencyType.EURO.toString(),
                AccountType.CURRENT.toString(),
                3
        );

        user = new User();
        user.setId(2);
        user.setUsername("user2");
        user.setEmail("email2@ex.com");
        user.setFirstName("second");
        user.setLastName("last");
        user.setPhoneNumber("0612345678");
        user.setDateOfBirth(LocalDate.of(2000, 9, 8));
        user.setPassword("Password1!");
        user.setRole(Role.CUSTOMER);
        user.setBsn("123456782");

        account = new Account();
        account.setType(AccountType.CURRENT);
        account.setBalance(1000);
        account.setIBAN("NL32INHO3125817743");
        account.setUser(user);
        account.setId(1);
        account.setCurrencyType(CurrencyType.EURO);
        account.setActive(true);

        user.setCurrentAccount(account);

        try {
            Field field = accountService.getClass().getDeclaredField("bankAccountIBAN");
            field.setAccessible(true);
            field.set(accountService, "NL01INHO0000000001");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testGetAccountById() throws AccountNotFoundException {
        Mockito.when(accountRepository.findById(account.getId())).thenReturn(java.util.Optional.of(account));

        Account foundAccount = accountService.getAccountById(account.getId());
        Assertions.assertEquals(account, foundAccount);
        Mockito.verify(accountRepository, Mockito.times(1)).findById(account.getId());
    }

    @Test
    void testGetAccountByIban() throws AccountNotFoundException {
        String validIban = account.getIBAN();
        String invalidIban = "1111";

        Mockito.when(accountRepository.findByIBAN(validIban)).thenReturn(Optional.of(account));
        Account resultValid = accountService.getAccountByIBAN(validIban);
        Mockito.verify(accountRepository, Mockito.times(1)).findByIBAN(validIban);

        Assertions.assertEquals(account, resultValid);

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            accountService.getAccountByIBAN(invalidIban);
        });
    }

    @Test
    void testAddAccount() {
        // Set up the mock objects and data
        Mockito.when(userService.getUserById(accountRequest.userId())).thenReturn(user);
        Mockito.when(accountRepository.save(Mockito.any(Account.class))).thenReturn(account);

        // Call the addAccount method
        Account createdAccount = accountService.addAccount(accountRequest);

        // Verify that the userService.getUserById method was called with the expected userId
        Mockito.verify(userService, Mockito.times(1)).getUserById(accountRequest.userId());

        // Verify that the accountRepository.save method was called with any Account object
        Mockito.verify(accountRepository, Mockito.times(1)).save(Mockito.any(Account.class));

        // Assert that the returned account matches the mock account
        Assertions.assertEquals(account, createdAccount);
    }






    @Test
    void testAddAccountWithInvalidAccountType() {
        accountRequest = new AccountRequest(
                CurrencyType.EURO.toString(),
                "INVALID",
                3
        );

        Mockito.when(userService.getUserById(accountRequest.userId())).thenReturn(user);
        Mockito.when(accountRepository.save(Mockito.any(Account.class))).thenReturn(account);

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            accountService.addAccount(accountRequest);
        });
    }

    @Test
    void testAddAccountWithInvalidCurrencyType() {
        accountRequest = new AccountRequest(
                "INVALID",
                AccountType.CURRENT.toString(),
                3
        );

        Mockito.when(userService.getUserById(accountRequest.userId())).thenReturn(user);
        Mockito.when(accountRepository.save(Mockito.any(Account.class))).thenReturn(account);

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            accountService.addAccount(accountRequest);
        });
    }

    @Test
    void testAddSavingAccountWithoutCurrentAccountShouldThrowIllegalArgumentException() {
        accountRequest = new AccountRequest(
                CurrencyType.EURO.toString(),
                AccountType.SAVING.toString(),
                3
        );

        Mockito.when(userService.getUserById(accountRequest.userId())).thenReturn(user);
        Mockito.when(accountRepository.save(Mockito.any(Account.class))).thenReturn(account);

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            accountService.addAccount(accountRequest);
        });
    }


    @Test
    void testDoesUserHaveAccountType() {
        AccountType accountType = AccountType.CURRENT;

        User user = new User();
        user.setId(2);
        Account account = new Account();
        account.setType(accountType);
        account.setUser(user);
        List<Account> accounts = new ArrayList<>();
        accounts.add(account);

        Mockito.when(accountRepository.findAllByUser(user)).thenReturn(accounts);

        boolean hasAccountType = accountService.doesUserHaveAccountType(user, accountType);

        Assertions.assertTrue(hasAccountType);
    }


    @Test
    void testDoesUserHaveAccountType_UserHasCurrentType_ReturnsTrue() {
        // Create a mock account with the desired account type
        Account currentAccount = new Account();
        currentAccount.setType(AccountType.CURRENT);

        // Create a mock list of accounts with the current account
        List<Account> accounts = new ArrayList<>();
        accounts.add(currentAccount);

        // Mock the behavior of getAccountsByUserId to return the mock list of accounts
        Mockito.when(accountService.getAccountsByUserId(user)).thenReturn(accounts);

        // Test if the user has an account of type CURRENT
        boolean resultTrue = accountService.doesUserHaveAccountType(user, AccountType.CURRENT);
        Assertions.assertTrue(resultTrue, "User should have an account of type CURRENT");

        // Test if the user has an account of type SAVING
        boolean resultFalse = accountService.doesUserHaveAccountType(user, AccountType.SAVING);
        Assertions.assertFalse(resultFalse, "User should not have an account of type SAVING");
    }



    @Test
    void testActivateOrDeactivateTheAccount() {
        AccountActiveRequest accountActiveRequest = new AccountActiveRequest(true);

        //Mocking
        Mockito.when(accountRepository.save(Mockito.any(Account.class))).thenReturn(account);

        accountService.activateOrDeactivateTheAccount(account, accountActiveRequest);

        Assertions.assertEquals(accountActiveRequest.isActive(), account.isActive());
        Mockito.verify(accountRepository, Mockito.times(1)).save(account);

    }

    @Test
    void testActivateOrDeactivateTheAccountWithInvalidAccountActiveRequest() {
        AccountActiveRequest accountActiveRequest = new AccountActiveRequest(false);
        //Mocking
        Mockito.when(accountRepository.save(Mockito.any(Account.class))).thenReturn(account);
        accountService.activateOrDeactivateTheAccount(account, accountActiveRequest);

        Assertions.assertEquals(accountActiveRequest.isActive(), account.isActive());
        Mockito.verify(accountRepository, Mockito.times(1)).save(account);
    }


    @Test
    void testGetAllAccountsByUserId() {
        List<Account> accounts = new ArrayList<>();
        accounts.add(account);

        Mockito.when(accountRepository.findAllByUser(user)).thenReturn(accounts);


        List<Account> foundAccounts = accountService.getAccountsByUserId(user);

        Assertions.assertEquals(accounts, foundAccounts);
        Mockito.verify(accountRepository, Mockito.times(1)).findAllByUser(user);
    }

    @Test
    void testIsActive() {
        Account activeAccount = new Account();
        activeAccount.setActive(true);

        Account inactiveAccount = new Account();
        inactiveAccount.setActive(false);

        boolean activeTrue = accountService.isActive(activeAccount);
        boolean activeFalse = accountService.isActive(inactiveAccount);

        Assertions.assertTrue(activeTrue);
        Assertions.assertFalse(activeFalse);
    }

    @Test
    void testUpdateAccount() {
        accountService.updateAccount(account);

        Mockito.verify(accountRepository, Mockito.times(1)).save(account);
    }

    @Test
    void assigningBankAccountToAdminShouldWork() {
        User user = new User();
        user.setId(1);
        user.setRole(Role.ADMIN);

        Mockito.when(accountRepository.findByIBAN(account.getIBAN())).thenReturn(Optional.empty());

        Assertions.assertDoesNotThrow(() ->accountService.addAccountForBank(user));
    }

    @Test
    void assigningBankAccountToNonAdminThrowsIllegalArgument() {
        User user = new User();
        user.setId(1);
        user.setRole(Role.CUSTOMER);

        Mockito.when(accountRepository.findByIBAN(account.getIBAN())).thenReturn(Optional.empty());

        Assertions.assertThrows(IllegalArgumentException.class, () -> accountService.addAccountForBank(user));
    }

    @Test
    void assigningBankAccountTwiceThrowsException() {
        User user = new User();
        user.setId(1);
        user.setRole(Role.ADMIN);

        Mockito.when(accountRepository.findByIBAN("NL01INHO0000000001")).thenReturn(Optional.of(account));

        Assertions.assertThrows(IllegalArgumentException.class, () -> accountService.addAccountForBank(user));
    }

    @Test
    void updateAbsoluteLimitsShouldWork() {
        AccountAbsoluteLimitRequest accountAbsoluteLimitRequest = new AccountAbsoluteLimitRequest(-10);
        Mockito.when(accountRepository.save(Mockito.any(Account.class))).thenReturn(account);

        Assertions.assertDoesNotThrow(() -> accountService.updateAbsoluteLimit(account, accountAbsoluteLimitRequest));
    }

    @Test
    void bankAccountCannotBeDeactivatedException() {
        AccountActiveRequest accountActiveRequest = new AccountActiveRequest(false);
        account.setIBAN("NL01INHO0000000001");
        Mockito.when(accountRepository.save(Mockito.any(Account.class))).thenReturn(account);

        Assertions.assertThrows(IllegalArgumentException.class, () -> accountService.activateOrDeactivateTheAccount(account, accountActiveRequest));
    }
}
