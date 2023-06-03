package nl.inholland.bank.services;

import nl.inholland.bank.configuration.ApiTestConfiguration;
import nl.inholland.bank.models.Limits;
import nl.inholland.bank.models.Role;
import nl.inholland.bank.models.Token;
import nl.inholland.bank.models.User;
import nl.inholland.bank.models.dtos.AuthDTO.LoginRequest;
import nl.inholland.bank.models.dtos.AuthDTO.RefreshTokenRequest;
import nl.inholland.bank.models.dtos.AuthDTO.jwt;
import nl.inholland.bank.models.dtos.UserDTO.UserForAdminRequest;
import nl.inholland.bank.models.dtos.UserDTO.UserRequest;
import nl.inholland.bank.repositories.AccountRepository;
import nl.inholland.bank.repositories.UserRepository;
import nl.inholland.bank.utils.JwtTokenProvider;
import org.hamcrest.Matchers;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.Assert;

import javax.naming.AuthenticationException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.any;

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
    private LoginRequest loginRequest;
    private Token token;
    private RefreshTokenRequest refreshTokenRequest;

    private int defaultDailyTransactionLimit = 100;
    private int defaultTransactionLimit = 1000;
    private int defaultAbsoluteLimit = 0;

    private int defaultGetAllUsersLimit = 20;

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
        loginRequest = new LoginRequest("user", "Password1!");
        token = new Token("jwt", 1234);
        refreshTokenRequest = new RefreshTokenRequest("refresh");

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

    @Test
    void addUserForAdminRequestAsUserShouldThrowException() throws AuthenticationException {
        Mockito.when(mockJwtTokenProvider.getRole()).thenReturn(Role.USER);
        Mockito.when(userRepository.findUserByUsername(userRequest.getUsername())).thenReturn(Optional.empty()).thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(user)).thenReturn(user);
        Mockito.when(bCryptPasswordEncoder.encode(user.getPassword())).thenReturn("fu942hjf89!$(@*9jfj489HJ*F9498");

        Exception e = Assertions.assertThrows(AuthenticationException.class, () -> userService.addUser(userForAdminRequest));
        Assertions.assertEquals("You are not authorized to create accounts with roles. Remove 'role' from request body.", e.getMessage());
    }

    @Test
    void addUserThatAlreadyExsitsThrowsIllegalArgumentException() throws AuthenticationException {
        Mockito.when(mockJwtTokenProvider.getRole()).thenReturn(Role.ADMIN);
        Mockito.when(userRepository.findUserByUsername(userRequest.getUsername())).thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(user)).thenReturn(user);
        Mockito.when(bCryptPasswordEncoder.encode(user.getPassword())).thenReturn("fu942hjf89!$(@*9jfj489HJ*F9498");

        Exception e = Assertions.assertThrows(IllegalArgumentException.class, () -> userService.addUser(userRequest));
        Assertions.assertEquals("Username already exists.", e.getMessage());
    }

    @Test
    void addUserWithInvalidPasswordReturnsIllegalArgumentException() throws AuthenticationException {
        Mockito.when(mockJwtTokenProvider.getRole()).thenReturn(Role.ADMIN);
        Mockito.when(userRepository.findUserByUsername(userRequest.getUsername())).thenReturn(Optional.empty()).thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(user)).thenReturn(user);
        Mockito.when(bCryptPasswordEncoder.encode(user.getPassword())).thenReturn("fu942hjf89!$(@*9jfj489HJ*F9498");

        userRequest.setPassword("password");

        Exception e = Assertions.assertThrows(IllegalArgumentException.class, () -> userService.addUser(userRequest));
        Assertions.assertEquals("Password does not meet requirements.", e.getMessage());
    }

    @Test
    void addUserWithoutBirthdateThrowsIllegalArgumentException() throws AuthenticationException {
        Mockito.when(mockJwtTokenProvider.getRole()).thenReturn(Role.ADMIN);
        Mockito.when(userRepository.findUserByUsername(userRequest.getUsername())).thenReturn(Optional.empty()).thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(user)).thenReturn(user);
        Mockito.when(bCryptPasswordEncoder.encode(user.getPassword())).thenReturn("fu942hjf89!$(@*9jfj489HJ*F9498");

        userRequest.setBirth_date(null);

        Exception e = Assertions.assertThrows(IllegalArgumentException.class, () -> userService.addUser(userRequest));
        Assertions.assertEquals("Birth date is required.", e.getMessage());
    }

    @Test
    void addUserWithInvalidBirthdateFormatThrowsIllegalArgumentException() throws AuthenticationException {
        Mockito.when(mockJwtTokenProvider.getRole()).thenReturn(Role.ADMIN);
        Mockito.when(userRepository.findUserByUsername(userRequest.getUsername())).thenReturn(Optional.empty()).thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(user)).thenReturn(user);
        Mockito.when(bCryptPasswordEncoder.encode(user.getPassword())).thenReturn("fu942hjf89!$(@*9jfj489HJ*F9498");

        userRequest.setBirth_date("200-09-08");

        Exception e = Assertions.assertThrows(IllegalArgumentException.class, () -> userService.addUser(userRequest));
        Assertions.assertEquals("Birth date must be in format yyyy-MM-dd", e.getMessage());
    }

    @Test
    void addAdminShouldReturnUser() throws AuthenticationException {
        Mockito.when(mockJwtTokenProvider.getRole()).thenReturn(Role.ADMIN);
        Mockito.when(userRepository.findUserByUsername(userRequest.getUsername())).thenReturn(Optional.empty()).thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(user)).thenReturn(user);
        Mockito.when(bCryptPasswordEncoder.encode(user.getPassword())).thenReturn("fu942hjf89!$(@*9jfj489HJ*F9498");

        User user = userService.addAdmin(userForAdminRequest);
        Assertions.assertEquals(user, this.user);
    }

    @Test
    void addAdminThatAlreadyExistsThrowsIllegalArgumentException() throws AuthenticationException {
        Mockito.when(mockJwtTokenProvider.getRole()).thenReturn(Role.ADMIN);
        Mockito.when(userRepository.findUserByUsername(userRequest.getUsername())).thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(user)).thenReturn(user);
        Mockito.when(bCryptPasswordEncoder.encode(user.getPassword())).thenReturn("fu942hjf89!$(@*9jfj489HJ*F9498");

        Exception e = Assertions.assertThrows(IllegalArgumentException.class, () -> userService.addAdmin(userForAdminRequest));
        Assertions.assertEquals("Username already exists.", e.getMessage());
    }

    @Test
    void addAdminWithInvalidPasswordReturnsIllegalArgumentException() throws AuthenticationException {
        Mockito.when(mockJwtTokenProvider.getRole()).thenReturn(Role.ADMIN);
        Mockito.when(userRepository.findUserByUsername(userRequest.getUsername())).thenReturn(Optional.empty()).thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(user)).thenReturn(user);
        Mockito.when(bCryptPasswordEncoder.encode(user.getPassword())).thenReturn("fu942hjf89!$(@*9jfj489HJ*F9498");

        userForAdminRequest.setPassword("password");

        Exception e = Assertions.assertThrows(IllegalArgumentException.class, () -> userService.addAdmin(userForAdminRequest));
        Assertions.assertEquals("Password does not meet requirements.", e.getMessage());
    }

    @Test
    void mappingStringUserShouldReturnRoleUser() {
        Role role = userService.mapStringToRole("user");
        Assertions.assertEquals(Role.USER, role);
    }

    @Test
    void mappingStringAdminShouldReturnRoleAdmin() {
        Role role = userService.mapStringToRole("admin");
        Assertions.assertEquals(Role.ADMIN, role);
    }

    @Test
    void mappingStringEmployeeShouldReturnRoleEmployee() {
        Role role = userService.mapStringToRole("employee");
        Assertions.assertEquals(Role.EMPLOYEE, role);
    }

    @Test
    void mappingInvalidStringShouldReturnInvalidRole() {
        Exception e = Assertions.assertThrows(IllegalArgumentException.class, () -> userService.mapStringToRole("invalid"));
        Assertions.assertEquals("Invalid role: INVALID", e.getMessage());
    }

    @Test
    void getAllUsersAsAdminOrEmployeeShouldReturnListOfUsers() {
        Optional<Integer> page = Optional.empty();
        Optional<Integer> limit = Optional.of(20);
        Optional<String> name = Optional.empty();
        Optional<Boolean> hasNoAccount = Optional.empty();
        Optional<Boolean> isActive = Optional.empty();

        int pageValue = page.orElse(0);
        int limitValue = limit.orElse(defaultGetAllUsersLimit);
        Pageable pageable = PageRequest.of(pageValue, limitValue);

        Mockito.when(mockJwtTokenProvider.getRole()).thenReturn(Role.ADMIN);
        Mockito.when(userRepository.findUsers(pageable, name, hasNoAccount, isActive)).thenReturn(new PageImpl<>(List.of(user)));
        Mockito.when(userLimitsService.getUserLimitsNoAuth(user.getId())).thenReturn(limits);
        List<User> users = userService.getAllUsers(page, limit, name, hasNoAccount, isActive);
        Assertions.assertEquals(List.of(user), users);
    }

    @Test
    void getAllUsersAsUserShouldReturnListOfUsers() {
        Optional<Integer> page = Optional.empty();
        Optional<Integer> limit = Optional.of(20);
        Optional<String> name = Optional.empty();
        Optional<Boolean> hasNoAccount = Optional.of(false);
        Optional<Boolean> isActive = Optional.of(true);

        int pageValue = page.orElse(0);
        int limitValue = limit.orElse(defaultGetAllUsersLimit);
        Pageable pageable = PageRequest.of(pageValue, limitValue);

        Mockito.when(mockJwtTokenProvider.getRole()).thenReturn(Role.USER);
        Mockito.when(userRepository.findUsers(pageable, name, hasNoAccount, isActive)).thenReturn(new PageImpl<>(List.of(user)));
        Mockito.when(userLimitsService.getUserLimitsNoAuth(user.getId())).thenReturn(limits);
        List<User> users = userService.getAllUsers(page, limit, name, hasNoAccount, isActive);
        Assertions.assertEquals(List.of(user), users);
    }

    @Test
    void getUserByIdShouldReturnUser() {
        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        Mockito.when(userLimitsService.getUserLimitsNoAuth(user.getId())).thenReturn(limits);
        User user = userService.getUserById(this.user.getId());
        Assertions.assertEquals(user, this.user);
    }

    @Test
    void loginWithValidCredentialsReturnsToken() throws AuthenticationException{
        Mockito.when(userRepository.findUserByUsername(userRequest.getUsername())).thenReturn(Optional.of(user));
        Mockito.when(bCryptPasswordEncoder.matches(userRequest.getPassword(), user.getPassword())).thenReturn(true);
        Mockito.when(mockJwtTokenProvider.createToken(user.getUsername(), user.getRole())).thenReturn(token);

        Token token = userService.login(loginRequest);
        Assertions.assertEquals(token, this.token);
    }

    @Test
    void loginToInactiveUserThrowsDisabledException() {
        Mockito.when(userRepository.findUserByUsername(userRequest.getUsername())).thenReturn(Optional.of(user));
        Mockito.when(bCryptPasswordEncoder.matches(userRequest.getPassword(), user.getPassword())).thenReturn(true);
        user.setActive(false);

        Exception e = Assertions.assertThrows(DisabledException.class, () -> userService.login(loginRequest));
        Assertions.assertEquals("User has been deactivated. Please contact customer support.", e.getMessage());
    }

    @Test
    void loginWithNotMatchinPasswordThrowsAuthenticationException() {
        Mockito.when(userRepository.findUserByUsername(userRequest.getUsername())).thenReturn(Optional.of(user));
        Mockito.when(bCryptPasswordEncoder.matches(userRequest.getPassword(), user.getPassword())).thenReturn(false);

        Exception e = Assertions.assertThrows(AuthenticationException.class, () -> userService.login(loginRequest));
        Assertions.assertEquals("Password incorrect", e.getMessage());
    }

    @Test
    void createRefreshTokenReturnNewRefreshToken() throws AuthenticationException {
        Mockito.when(userRepository.findUserByUsername(userRequest.getUsername())).thenReturn(Optional.of(user));
        Mockito.when(mockJwtTokenProvider.createRefreshToken(user.getUsername())).thenReturn("refresh token");

        String refreshToken = userService.createRefreshToken(userRequest.getUsername());
        Assertions.assertEquals("refresh token", refreshToken);
    }

    @Test
    void refreshingTokenReturnsJwt() throws AuthenticationException {
        Mockito.when(mockJwtTokenProvider.refreshTokenUsername(refreshTokenRequest.refresh_token())).thenReturn("user");
        Mockito.when(userRepository.findUserByUsername(userRequest.getUsername())).thenReturn(Optional.of(user));
        Mockito.when(mockJwtTokenProvider.createToken(user.getUsername(), user.getRole())).thenReturn(token);
        Mockito.when(mockJwtTokenProvider.createRefreshToken(user.getUsername())).thenReturn("refresh token");

        jwt token = userService.refresh(refreshTokenRequest);
        Assertions.assertEquals(this.token.jwt(), token.access_token());
    }

    @Test
    void refreshingTokenForInactiveAccountThrowsDisabledException() throws AuthenticationException {
        Mockito.when(mockJwtTokenProvider.refreshTokenUsername(refreshTokenRequest.refresh_token())).thenReturn("user");
        Mockito.when(userRepository.findUserByUsername(userRequest.getUsername())).thenReturn(Optional.of(user));
        Mockito.when(mockJwtTokenProvider.createToken(user.getUsername(), user.getRole())).thenReturn(token);
        Mockito.when(mockJwtTokenProvider.createRefreshToken(user.getUsername())).thenReturn("refresh token");

        user.setActive(false);

        Exception e = Assertions.assertThrows(DisabledException.class, () -> userService.refresh(refreshTokenRequest));
        Assertions.assertEquals("User has been deactivated. Please contact customer support.", e.getMessage());
    }

    @Test
    void getBearerUsernameReturnsString() {
        Mockito.when(mockJwtTokenProvider.getUsername()).thenReturn("user");
        String username = userService.getBearerUsername();
        Assertions.assertEquals("user", username);
    }

    @Test
    void providingValidPasswordReturnTrue() {
        Assertions.assertTrue(userService.isPasswordValid("Password123!"));
    }

    @Test
    void providingInvalidPasswordReturnsFalse() {
        Assertions.assertFalse(userService.isPasswordValid("password"));
    }

    @Test
    void providingRepeatingPasswordReturnsFalse() {
        Assertions.assertFalse(userService.isPasswordValid("aaaaaaaaaaaaa"));
    }

    @Test
    void providingTooShortPaswordReturnFalse() {
        Assertions.assertFalse(userService.isPasswordValid("Pass1!"));
    }
}
