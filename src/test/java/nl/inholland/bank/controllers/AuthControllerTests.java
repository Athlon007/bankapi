package nl.inholland.bank.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.inholland.bank.configuration.ApiTestConfiguration;
import nl.inholland.bank.models.Role;
import nl.inholland.bank.models.Token;
import nl.inholland.bank.models.User;
import nl.inholland.bank.models.dtos.AuthDTO.LoginRequest;
import nl.inholland.bank.models.dtos.AuthDTO.RefreshTokenRequest;
import nl.inholland.bank.models.dtos.AuthDTO.jwt;
import nl.inholland.bank.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
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

@ExtendWith(SpringExtension.class)
@WebMvcTest(AuthController.class)
@Import(ApiTestConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private LoginRequest mockLoginRequest;
    private jwt mockJwt;
    private Token mockToken;
    private User mockUser;
    private RefreshTokenRequest mockRefreshTokenRequest;

    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        mockToken = new Token("token", 1234);
        mockLoginRequest = new LoginRequest("username", "password");
        mockJwt = new jwt("token", "refresh token", 1, 123456);
        mockRefreshTokenRequest = new RefreshTokenRequest("refresh token");

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
    void loginShouldReturnjwt() throws Exception {
        Mockito.when(userService.login(mockLoginRequest)).thenReturn(mockToken);
        Mockito.when(userService.getUserIdByUsername("user")).thenReturn(1);
        Mockito.when(userService.createRefreshToken("user")).thenReturn("refresh token");

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                .contentType("application/json")
                .content(mapper.writeValueAsString(mockLoginRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.access_token").value(mockJwt.access_token()));
    }

    @Test
    void loginWithInvalidCredentialShouldReturn401() throws Exception {
        Mockito.when(userService.login(mockLoginRequest)).thenThrow(new AuthenticationException());

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                .contentType("application/json")
                .content(mapper.writeValueAsString(mockLoginRequest)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    void refreshingTokenReturnsNewJwt() throws Exception {
        Mockito.when(userService.refresh(mockRefreshTokenRequest)).thenReturn(mockJwt);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/refresh")
                .contentType("application/json")
                .content(mapper.writeValueAsString(mockRefreshTokenRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.access_token").value(mockJwt.access_token()));
    }
}
