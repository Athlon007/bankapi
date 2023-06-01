package nl.inholland.bank.controllers;

import nl.inholland.bank.configuration.ApiTestConfiguration;
import nl.inholland.bank.controllers.UserController;
import nl.inholland.bank.models.Role;
import nl.inholland.bank.models.User;
import nl.inholland.bank.models.dtos.UserDTO.UserRequest;
import nl.inholland.bank.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
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
public class UserControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private User mockUser;

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
}
