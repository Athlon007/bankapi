package nl.inholland.bank.services;

import nl.inholland.bank.configuration.ApiTestConfiguration;
import nl.inholland.bank.models.Limits;
import nl.inholland.bank.models.Role;
import nl.inholland.bank.models.User;
import nl.inholland.bank.models.dtos.UserDTO.UserForAdminRequest;
import nl.inholland.bank.models.dtos.UserDTO.UserRequest;
import nl.inholland.bank.repositories.AccountRepository;
import nl.inholland.bank.repositories.UserRepository;
import nl.inholland.bank.utils.JwtTokenProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.naming.AuthenticationException;
import java.time.LocalDate;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@Import(ApiTestConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class UserServiceTests {

    private UserService userService;

    @MockBean
    private UserRepository userRepository;
    @MockBean
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @MockBean
    private AccountRepository accountRepository;
    @MockBean
    private UserLimitsService userLimitsService;
    @Autowired
    private JwtTokenProvider mockJwtTokenProvider;

    private User user;
    private UserRequest userRequest;
    private UserForAdminRequest userForAdminRequest;
    private Limits limits;

    private int defaultDailyTransactionLimit = 100;
    private int defaultTransactionLimit = 1000;
    private int defaultAbsoluteLimit = 0;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, bCryptPasswordEncoder, mockJwtTokenProvider, userLimitsService, accountRepository);

        user = new User();
        user.setId(1);
        user.setUsername("user");
        user.setEmail("email@ex.com");
        user.setFirstName("first");
        user.setLastName("last");
        user.setPhoneNumber("0612345678");
        user.setDateOfBirth(LocalDate.of(2000, 9, 8));
        user.setPassword("Password1!");
        user.setRole(Role.USER);
        user.setBsn("123456782");

        limits = new Limits();
        limits.setTransactionLimit(1000);
        limits.setDailyTransactionLimit(1000);
        limits.setAbsoluteLimit(0);
        limits.setRemainingDailyTransactionLimit(1000);

        userRequest = new UserRequest("email@ex.com", "user", "Password1!", "Firstly", "Fister", "820510026", "0612345678", "2000-09-08");
        userForAdminRequest = new UserForAdminRequest("email@ex.com", "user", "Password1!", "Firstly", "Fister", "820510026", "0612345678", "2000-09-08", "user");

        Limits defaultLimits = new Limits();
        defaultLimits.setDailyTransactionLimit(this.defaultDailyTransactionLimit);
        defaultLimits.setTransactionLimit(this.defaultTransactionLimit);
        defaultLimits.setAbsoluteLimit(this.defaultAbsoluteLimit);
        defaultLimits.setRemainingDailyTransactionLimit(this.defaultDailyTransactionLimit);

        Mockito.when(userLimitsService.getDefaultLimits()).thenReturn(defaultLimits);
    }

    @Test
    void addUserAsAdminShouldReturnUser() throws AuthenticationException {
        Mockito.when(mockJwtTokenProvider.getRole()).thenReturn(Role.ADMIN);
        Mockito.when(userRepository.findUserByUsername(userRequest.getUsername())).thenReturn(Optional.empty()).thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(user)).thenReturn(user);
        Mockito.when(bCryptPasswordEncoder.encode(user.getPassword())).thenReturn("fu942hjf89!$(@*9jfj489HJ*F9498");

        User user = userService.addUser(userRequest);
        Assertions.assertEquals(user, this.user);
    }
}
