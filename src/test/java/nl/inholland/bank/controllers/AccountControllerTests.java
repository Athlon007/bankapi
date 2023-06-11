package nl.inholland.bank.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.inholland.bank.configuration.ApiTestConfiguration;
import nl.inholland.bank.models.*;
import nl.inholland.bank.models.dtos.AccountDTO.AccountAbsoluteLimitRequest;
import nl.inholland.bank.models.dtos.AccountDTO.AccountActiveRequest;
import nl.inholland.bank.models.dtos.AccountDTO.AccountClientResponse;
import nl.inholland.bank.models.dtos.AccountDTO.AccountRequest;
import nl.inholland.bank.services.AccountService;
import nl.inholland.bank.services.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@WebMvcTest(AccountController.class)
@Import(ApiTestConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class AccountControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @MockBean
    private UserService userService;

    private Account account;

    private Account account2;

    private AccountRequest accountRequest;

    private AccountActiveRequest accountActiveRequest;

    private AccountAbsoluteLimitRequest accountAbsoluteLimitRequest;

    User user = new User();

    User user2 = new User();

    User user3 = new User();

    private final ObjectMapper mapper = new ObjectMapper();


    @BeforeEach
    void setUp() {
        account = new Account();
        account.setId(1);
        user.setId(1);
        user.setUsername("user");
        user.setEmail("email@ex.com");
        user.setFirstName("first");
        user.setLastName("last");
        user.setPhoneNumber("0612345678");
        user.setDateOfBirth(LocalDate.of(2000, 9, 8));
        user.setPassword("Password1!");
        user.setRole(Role.CUSTOMER);
        user.setBsn("123456782");
        account.setUser(user);
        account.setBalance(100);
        account.setCurrencyType(CurrencyType.EURO);
        account.setIBAN("NL87INHO7354367047");
        account.setType(AccountType.CURRENT);
        account.setActive(true);
        account.setAbsoluteLimit(0);

        account2 = new Account();
        account2.setId(2);
        user2.setId(2);
        user2.setUsername("user2");
        user2.setEmail("emaiil@ex.com");
        user2.setFirstName("first");
        user2.setLastName("last");
        user2.setPhoneNumber("0642345678");
        user2.setDateOfBirth(LocalDate.of(2000, 9, 8));
        user2.setPassword("Password1!");
        user2.setRole(Role.CUSTOMER);
        user2.setBsn("038718352");
        account2.setUser(user2);
        account2.setBalance(100);
        account2.setCurrencyType(CurrencyType.EURO);
        account2.setIBAN("NL87INHO7354367047");
        account2.setType(AccountType.CURRENT);
        account2.setActive(true);
        account2.setAbsoluteLimit(0);

        user3.setId(3);
        user3.setUsername("user3");
        user3.setEmail("emaiill@ex.com");
        user3.setFirstName("first");
        user3.setLastName("last");
        user3.setPhoneNumber("0642345678");
        user3.setDateOfBirth(LocalDate.of(2000, 9, 8));
        user3.setPassword("Password1!");
        user3.setRole(Role.CUSTOMER);
        user3.setBsn("038718352");

        accountRequest = new AccountRequest(CurrencyType.EURO.toString(), AccountType.CURRENT.toString(), 1);
        accountActiveRequest = new AccountActiveRequest(false);
        accountAbsoluteLimitRequest = new AccountAbsoluteLimitRequest(-30);

        Mockito.when(userService.getUserById(account.getUser().getId())).thenReturn(user);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void gettingAllAccountsAsAdminShouldReturnListWithOneCurrentAccount() throws Exception {
        Mockito.when(accountService.getAccountsByUserId(user)).thenReturn(List.of(
                account
        ));

        Mockito.when(userService.getBearerUserRole()).thenReturn(Role.ADMIN);
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/accounts/1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(account.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].IBAN").value(account.getIBAN()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].currency_type").value(account.getCurrencyType().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].account_type").value(account.getType().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].isActive").value(account.isActive()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].balance").value(account.getBalance()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].absolute_limit").value(account.getAbsoluteLimit()));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void gettingAllAccountsAsEmployeeShouldReturnListWithOneCurrentAccount() throws Exception {
        Mockito.when(accountService.getAccountsByUserId(user)).thenReturn(List.of(
                account
        ));

        Mockito.when(userService.getBearerUserRole()).thenReturn(Role.EMPLOYEE);
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/accounts/1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(account.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].IBAN").value(account.getIBAN()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].currency_type").value(account.getCurrencyType().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].account_type").value(account.getType().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].isActive").value(account.isActive()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].balance").value(account.getBalance()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].absolute_limit").value(account.getAbsoluteLimit()));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void gettingAllAccountAsOwnerShouldReturnListWithOneCurrentAccount() throws Exception {
        Mockito.when(accountService.getAccountsByUserId(user)).thenReturn(List.of(
                account
        ));

        Mockito.when(userService.getBearerUserRole()).thenReturn(Role.CUSTOMER);  // Set the appropriate role of the user
        Mockito.when(userService.getBearerUsername()).thenReturn("user");  // Set the username of the owner

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/accounts/1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(account.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].IBAN").value(account.getIBAN()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].currency_type").value(account.getCurrencyType().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].account_type").value(account.getType().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].isActive").value(account.isActive()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].balance").value(account.getBalance()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].absolute_limit").value(account.getAbsoluteLimit()));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void gettingEmptyAccountsListShouldReturnBadRequest() throws Exception {
        Mockito.when(accountService.getAccountsByUserId(user)).thenReturn(Collections.emptyList());
        Mockito.when(userService.getBearerUserRole()).thenReturn(Role.ADMIN);
        Mockito.when(userService.getBearerUsername()).thenReturn("admin");

        mockMvc.perform(MockMvcRequestBuilders.get("/accounts/3"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error_message").value("No account found"));
    }



    @Test
    @WithMockUser(username = "user2", roles = {"USER"})
    void gettingAnotherUserAccountAsOwnerShouldReturnUnauthorized() throws Exception {
        Mockito.when(accountService.getAccountsByUserId(user)).thenReturn(List.of(
                account
        ));

        Mockito.when(userService.getBearerUserRole()).thenReturn(Role.CUSTOMER);  // Set the appropriate role of the user
        Mockito.when(userService.getBearerUsername()).thenReturn("user2");  // Set the username of a non-owner


        mockMvc.perform(
                        MockMvcRequestBuilders.get("/accounts/1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.error_message").value("Unauthorized request"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void loggedInUserAndAccountOwnerNotSame() {
        User accountOwner = account.getUser();
        User loggedInUser = userService.getUserById(userService.getUserIdByUsername(userService.getBearerUsername()));
    }


    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void addAccountShouldReturnCreated() throws Exception {
        Mockito.when(accountService.addAccount(accountRequest)).thenReturn(account);
        Mockito.when(userService.getBearerUserRole()).thenReturn(Role.ADMIN);

        MockHttpServletRequestBuilder post = MockMvcRequestBuilders.post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(accountRequest))
                .header("Authorization", "Bearer " + "token");

        mockMvc.perform(post)
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(account.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.IBAN").value(account.getIBAN()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.currency_type").value(account.getCurrencyType().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.account_type").value(account.getType().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.isActive").value(account.isActive()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance").value(account.getBalance()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.absolute_limit").value(account.getAbsoluteLimit()));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void addAccountShouldAsUserReturnUnauthorized() throws Exception {
        Mockito.when(accountService.addAccount(accountRequest)).thenReturn(account);
        Mockito.when(userService.getBearerUserRole()).thenReturn(Role.CUSTOMER);
        MockHttpServletRequestBuilder post = MockMvcRequestBuilders.post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(accountRequest))
                .header("Authorization", "Bearer " + "token");

        mockMvc.perform(post)
                .andExpect(MockMvcResultMatchers.jsonPath("$.error_message").value("Unauthorized request"));

    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void activateAccountShouldReturnActivatedUser() throws Exception {
        Mockito.when(accountService.getAccountById(account.getId())).thenReturn(account);
        Mockito.when(accountService.activateOrDeactivateTheAccount(account, accountActiveRequest)).thenReturn(account);
        Mockito.when(userService.getBearerUserRole()).thenReturn(Role.ADMIN);

        mockMvc.perform(MockMvcRequestBuilders.put("/accounts/1/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(accountActiveRequest))
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.isActive").value(account.isActive()));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void activateAccountAsUserShouldReturnUnauthorized() throws Exception {
        Mockito.when(accountService.getAccountById(account.getId())).thenReturn(account);
        Mockito.when(accountService.activateOrDeactivateTheAccount(account, accountActiveRequest)).thenReturn(account);

        mockMvc.perform(MockMvcRequestBuilders.put("/accounts/1/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(accountActiveRequest))
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$.error_message").value("Unauthorized request"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void activateAnAccountThatDoesNotBelongToSameUserShouldReturnUnauthorized() throws Exception {
        Mockito.when(accountService.getAccountById(account.getId())).thenReturn(account);
        Mockito.when(userService.getUserById(2)).thenReturn(user2);
        Mockito.when(accountService.activateOrDeactivateTheAccount(account, accountActiveRequest)).thenReturn(account);
        Mockito.when(userService.getBearerUserRole()).thenReturn(Role.ADMIN);
        Mockito.when(userService.getBearerUsername()).thenReturn("admin");

        mockMvc.perform(MockMvcRequestBuilders.put("/accounts/2/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(accountActiveRequest))
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$.error_message").value("Unauthorized request"));
    }


    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void updateAccountAbsoluteLimitAsAdminShouldReturnAccount() throws Exception {
        Mockito.when(accountService.getAccountById(account.getId())).thenReturn(account);
        Mockito.when(accountService.updateAbsoluteLimit(account, accountAbsoluteLimitRequest)).thenReturn(account);
        Mockito.when(userService.getBearerUserRole()).thenReturn(Role.ADMIN);

        mockMvc.perform(MockMvcRequestBuilders.put("/accounts/1/1/limit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(accountAbsoluteLimitRequest))
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.absolute_limit").value(account.getAbsoluteLimit()));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void updateAccountAbsoluteLimitAsAdminShouldReturnUnauthorized() throws Exception{
        Mockito.when(accountService.getAccountById(account.getId())).thenReturn(account);
        Mockito.when(accountService.activateOrDeactivateTheAccount(account, accountActiveRequest)).thenReturn(account);
        Mockito.when(userService.getBearerUserRole()).thenReturn(Role.CUSTOMER);

        mockMvc.perform(MockMvcRequestBuilders.put("/accounts/1/1/limit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(accountActiveRequest))
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$.error_message").value("Unauthorized request"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getAccountsWithParams() throws Exception {
        Mockito.when(accountService.getAccounts(Optional.of(0), Optional.of(10), Optional.of(account.getIBAN()), Optional.of("John"), Optional.of("Doe"), Optional.of("CURRENT")))
                .thenReturn(List.of(account));

        Mockito.when(userService.getBearerUserRole()).thenReturn(Role.ADMIN);

        mockMvc.perform(MockMvcRequestBuilders.get("/accounts", 0, 10, account.getIBAN(), "John", "Doe", "CURRENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(accountActiveRequest))
                )
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getAccountsWithLowercaseIban() throws Exception {
        Mockito.when(accountService.getAccounts(Optional.of(0), Optional.of(10), Optional.of(account.getIBAN()), Optional.of("John"), Optional.of("Doe"), Optional.of("CURRENT")))
                .thenReturn(List.of(account));

        Mockito.when(userService.getBearerUserRole()).thenReturn(Role.ADMIN);

        String iban = account.getIBAN().toLowerCase();

        mockMvc.perform(MockMvcRequestBuilders.get("/accounts?iban=" + iban)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(accountActiveRequest))
                )
                .andExpect(MockMvcResultMatchers.status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.get("/accounts", 0, 10, account.getIBAN(), "John", "Doe", "CURRENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(accountActiveRequest))
                )
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"CUSTOMER"})
    void getAccountsWithParamsAsCustomer() throws Exception {
        Mockito.when(accountService.getAccounts(Optional.of(0), Optional.of(10), Optional.of(account.getIBAN()), Optional.of("John"), Optional.of("Doe"), Optional.of("CURRENT")))
                .thenReturn(List.of(account));

        Mockito.when(userService.getBearerUserRole()).thenReturn(Role.CUSTOMER);

        mockMvc.perform(MockMvcRequestBuilders.get("/accounts", 0, 10, account.getIBAN(), "John", "Doe", "CURRENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(accountActiveRequest))
                )
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void buildAccountClientResponse() {
        AccountController accountController = new AccountController(accountService, userService);
        AccountClientResponse response = accountController.buildAccountClientResponse(account);
        Assertions.assertEquals(account.getIBAN(), response.IBAN());
    }
}
