package nl.inholland.bank.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONUtil;
import nl.inholland.bank.configuration.ApiTestConfiguration;
import nl.inholland.bank.controllers.UserController;
import nl.inholland.bank.models.*;
import nl.inholland.bank.models.dtos.UserDTO.UserForAdminRequest;
import nl.inholland.bank.models.dtos.UserDTO.UserLimitsRequest;
import nl.inholland.bank.models.dtos.UserDTO.UserRequest;
import nl.inholland.bank.services.UserLimitsService;
import nl.inholland.bank.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.naming.AuthenticationException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ExtendWith(SpringExtension.class)
@WebMvcTest(UserController.class)
@Import(ApiTestConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;
    @MockBean
    private UserLimitsService userLimitsService;

    private User mockUser;
    private UserRequest mockUserRequest;
    private UserForAdminRequest mockUserForAdminRequest;
    private UserLimitsRequest mockUserLimitsRequest;

    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1);
        mockUser.setUsername("user");
        mockUser.setEmail("email@ex.com");
        mockUser.setFirstName("first");
        mockUser.setLastName("last");
        mockUser.setPhoneNumber("0612345678");
        mockUser.setDateOfBirth(LocalDate.of(2000, 9, 8));
        mockUser.setPassword("Password1!");
        mockUser.setRole(Role.USER);
        mockUser.setBsn("123456782");

        Limits limits = new Limits();
        limits.setTransactionLimit(1000);
        limits.setDailyTransactionLimit(1000);
        limits.setRemainingDailyTransactionLimit(1000);
        mockUser.setLimits(limits);

        Account currentAccount = new Account();
        currentAccount.setType(AccountType.CURRENT);
        currentAccount.setBalance(1000);
        currentAccount.setIBAN("NL62INHO2395766879");
        currentAccount.setUser(mockUser);
        currentAccount.setId(1);
        currentAccount.setCurrencyType(CurrencyType.EURO);
        currentAccount.setActive(true);
        currentAccount.setAbsoluteLimit(0);
        mockUser.setCurrentAccount(currentAccount);

        Account savingAccount = new Account();
        savingAccount.setType(AccountType.SAVING);
        savingAccount.setBalance(1000);
        savingAccount.setIBAN("NL04INHO2539494278");
        savingAccount.setUser(mockUser);
        savingAccount.setId(1);
        savingAccount.setCurrencyType(CurrencyType.EURO);
        savingAccount.setAbsoluteLimit(0);
        mockUser.setSavingAccount(savingAccount);

        mockUserRequest = new UserRequest("email@ex.com", "user", "Password1!", "Firstly", "Fister", "820510026", "0612345678", "2000-09-08");
        mockUserForAdminRequest = new UserForAdminRequest("email@mail.com", "user2", "Password1!", "Firstly", "Fister", "820510026", "0612345678", "2000-09-08", "employee");
        mockUserLimitsRequest = new UserLimitsRequest(1000, 1000);
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void gettingAllUsersAsAdminShouldReturnListWithOneUser() throws Exception {
        Mockito.when(userService.getAllUsers(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()))
                .thenReturn(List.of(
                        mockUser
                ));

        Mockito.when(userService.getBearerUserRole()).thenReturn(Role.ADMIN);

        mockMvc.perform(MockMvcRequestBuilders.get("/users"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].role").exists());
    }

    @Test
    @WithMockUser(username = "user" )
    void gettingAllUsersAsUserShouldReturnListWithOneUser() throws Exception {
        Mockito.when(userService.getAllUsers(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()))
                .thenReturn(List.of(
                        mockUser
                ));

        Mockito.when(userService.getBearerUserRole()).thenReturn(Role.USER);

        mockMvc.perform(MockMvcRequestBuilders.get("/users"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].role").doesNotExist());
    }

    @Test
    @WithMockUser(username = "user", roles = { "ADMIN" })
    void gettingUserByIdShouldReturnUser() throws Exception {
        Mockito.when(userService.getUserById(1)).thenReturn(mockUser);
        Mockito.when(userService.getBearerUserRole()).thenReturn(Role.ADMIN);

        mockMvc.perform(MockMvcRequestBuilders.get("/users/1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.role").exists());
    }

    @Test
    @WithMockUser(username = "user", roles = { "USER" })
    void gettingUserByIdOfOtherUserAsUserShouldReturnUser() throws Exception {
        Mockito.when(userService.getUserById(1)).thenReturn(mockUser);
        Mockito.when(userService.getBearerUsername()).thenReturn("otherguy");
        Mockito.when(userService.getBearerUserRole()).thenReturn(Role.USER);

        mockMvc.perform(MockMvcRequestBuilders.get("/users/1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.role").doesNotExist());
    }

    @Test
    @WithMockUser(username = "user")
    void userGettingOwnLimitsShouldReturnLimits() throws Exception {
        Mockito.when(userLimitsService.getUserLimits(1)).thenReturn(mockUser.getLimits());

        mockMvc.perform(MockMvcRequestBuilders.get("/users/1/limits"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.transaction_limit").value(1000))
                .andExpect(jsonPath("$.daily_transaction_limit").value(1000))
                .andExpect(jsonPath("$.remaining_daily_transaction_limit").value(1000));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void addingNewUserShouldReturnUser() throws Exception {
        Mockito.when(userService.addUser(mockUserForAdminRequest)).thenReturn(mockUser);
        Mockito.when(userService.getBearerUserRole()).thenReturn(Role.ADMIN);

        MockHttpServletRequestBuilder post = MockMvcRequestBuilders.post("/users")
                .contentType("application/json")
                .content(mapper.writeValueAsString(mockUserForAdminRequest))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + "token");

        mockMvc.perform(post)
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.role").exists());


    }

    @Test
    @WithMockUser(username = "guest")
    void addingNewUserAsGuestShouldReturnUser() throws Exception {
        Mockito.when(userService.addUser(mockUserRequest)).thenReturn(mockUser);
        Mockito.when(userService.getBearerUserRole()).thenReturn(null);

        MockHttpServletRequestBuilder post = MockMvcRequestBuilders.post("/users")
                .contentType("application/json")
                .content(mapper.writeValueAsString(mockUserRequest))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + "token");

        mockMvc.perform(post)
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(username = "user", roles = { "USER" })
    void addingNewUserWithoutBodyReturnsBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                .contentType("application/json")
                .content("{}")
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void updatingUserAsAdminReturnsUpdatedUser() throws Exception {
        Mockito.when(userService.updateUser(1, mockUserForAdminRequest)).thenReturn(mockUser);

        mockMvc.perform(MockMvcRequestBuilders.put("/users/1")
                .contentType("application/json")
                .content(mapper.writeValueAsString(mockUserForAdminRequest))
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.role").exists());
    }

    @Test
    @WithMockUser(username = "user", roles = { "user" })
    void updatingUserAsUserReturnsUpdatedUser() throws Exception {
        Mockito.when(userService.updateUser(1, mockUserRequest)).thenReturn(mockUser);

        mockMvc.perform(MockMvcRequestBuilders.put("/users/1")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(mockUserRequest))
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.role").exists());
    }

    @Test
    @WithMockUser(username = "employee", roles = { "EMPLOYEE"})
    void deletingUserReturns200Response() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/users/1"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(username = "employee", roles = { "EMPLOYEE"})
    void updatingUserLimitsReturnsUserLimits() throws Exception {
        Mockito.when(userLimitsService.updateUserLimits(1, mockUserLimitsRequest)).thenReturn(mockUser.getLimits());

        mockMvc.perform(MockMvcRequestBuilders.put("/users/1/limits")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(mockUserLimitsRequest))
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.transaction_limit").value(1000))
                .andExpect(jsonPath("$.daily_transaction_limit").value(1000))
                .andExpect(jsonPath("$.remaining_daily_transaction_limit").value(1000));
    }
}

