package nl.inholland.bank.services;

import nl.inholland.bank.models.Limits;
import nl.inholland.bank.models.Role;
import nl.inholland.bank.models.User;
import nl.inholland.bank.models.dtos.*;
import nl.inholland.bank.repositories.UserRepository;
import nl.inholland.bank.utils.JwtTokenProvider;
import javax.naming.AuthenticationException;

import org.hibernate.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    protected final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${bankapi.application.request.limits}")
    private int defaultGetAllUsersLimit;

    @Value("${bankapi.user.defaults.dailyTransactionLimit}")
    private int defaultDailyTransactionLimit;
    @Value("${bankapi.user.defaults.transactionLimit}")
    private int defaultTransactionLimit;
    @Value("${bankapi.user.defaults.absoluteLimit}")
    private int defaultAbsoluteLimit;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public User addUser(UserRequest userRequest) throws AuthenticationException {
        if (userRepository.findUserByUsername(userRequest.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists.");
        }

        if (!isPasswordValid(userRequest.getPassword())) {
            throw new IllegalArgumentException("Password does not meet requirements.");
        }

        // If current token bearer is not an admin, and userRequest is type of UserForAdminRequest, throw exception.
        if (getBearerUserRole() != Role.ADMIN && userRequest instanceof UserForAdminRequest) {
            throw new AuthenticationException("You are not authorized to create accounts with roles. Remove 'role' from request body.");
        }

        User user = mapUserRequestToUser(userRequest);
        user.setLimits(this.getDefaultLimits());
        userRepository.save(user);
        return userRepository.findUserByUsername(user.getUsername()).orElseThrow(() -> new ObjectNotFoundException(user.getId(), "User"));
    }

    // This function is called during initial setup of the application.
    // it is similar to addUser, but skips the role check.
    // Should not be exposed to API.
    public User addAdmin(UserForAdminRequest userRequest) {
        if (userRepository.findUserByUsername(userRequest.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists.");
        }

        if (!isPasswordValid(userRequest.getPassword())) {
            throw new IllegalArgumentException("Password does not meet requirements.");
        }

        User user = mapUserRequestToUser(userRequest);
        user.setLimits(this.getDefaultLimits());
        userRepository.save(user);
        return userRepository.findUserByUsername(user.getUsername()).orElseThrow(() -> new ObjectNotFoundException(user.getId(), "User"));
    }

    public List<User> getAllUsers(Optional<Integer> page, Optional<Integer> limit, Optional<String> name, Optional<Boolean> hasNoAccounts) {
        // If user has role ADMIN, return all users.
        // Otherwise, return only users that have accounts.

        // Limit may be not present, so we need to check for that.
        int pageValue = page.orElse(0);
        int limitValue = limit.orElse(defaultGetAllUsersLimit);

        Role userRole = getBearerUserRole();

        // Declare pageable, so we can limit the results.
        Pageable pageable = PageRequest.of(pageValue, limitValue);

        // TODO: hasNoAccounts
        // TODO: Calculate remaining limits for today.

        if (userRole == Role.ADMIN || userRole == Role.EMPLOYEE) {
            return name.map(
                    s -> userRepository.findAllByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(s, s, pageable).getContent())
                    .orElseGet(() -> userRepository.findAll(pageable).getContent()
                    );
        }

        // FIXME: This should return all users that HAVE accounts, even if they are an employee.
        return name.map(
                s -> userRepository.findAllByRoleAndFirstNameContainingIgnoreCaseOrRoleAndLastNameContainingIgnoreCase(Role.USER, s, Role.USER, s, pageable).getContent())
                .orElseGet(() -> userRepository.findAllByRole(Role.USER, pageable).getContent()
                );
    }

    public User getUserById(int id) {
        return userRepository.findById(id).orElseThrow(()-> new ObjectNotFoundException(id, "User not found"));
    }

    public String login(LoginRequest loginRequest) throws AuthenticationException {
        User user = userRepository.findUserByUsername(loginRequest.username())
                .orElseThrow(() -> new AuthenticationException("Username not found"));

        if (!bCryptPasswordEncoder.matches(loginRequest.password(), user.getPassword()))
            throw new AuthenticationException("Password incorrect");

        return jwtTokenProvider.createToken(user.getUsername(), user.getRole());
    }

    public String createRefreshToken(String username) throws AuthenticationException {
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new AuthenticationException("Username not found"));

        return jwtTokenProvider.createRefreshToken(user.getUsername());
    }

    public jwt refresh(RefreshTokenRequest refreshTokenRequest) throws AuthenticationException {
        // Check if it's not expired
        String username = jwtTokenProvider.refreshTokenUsername(refreshTokenRequest.refresh_token());
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new AuthenticationException("Username not found"));

        return new jwt(jwtTokenProvider.createToken(username, user.getRole()), jwtTokenProvider.createRefreshToken(username));
    }

    public User mapUserRequestToUser(UserRequest userRequest) {
        User user = new User();
        user.setFirstName(userRequest.getFirst_name());
        user.setLastName(userRequest.getLast_name());
        user.setEmail(userRequest.getEmail());
        user.setBsn(userRequest.getBsn());
        user.setPhoneNumber(userRequest.getPhone_number());
        // Convert string of format "yyyy-MM-dd" to LocalDate
        if (userRequest.getBirth_date() == null) {
            throw new IllegalArgumentException("Birth date is required.");
        }
        LocalDate dateOfBirth = LocalDate.parse(userRequest.getBirth_date());
        user.setDateOfBirth(dateOfBirth);
        user.setUsername(userRequest.getUsername());
        user.setPassword(bCryptPasswordEncoder.encode(userRequest.getPassword()));
        user.setRole(Role.USER);
        if (userRequest instanceof UserForAdminRequest) {
            user.setRole(mapStringToRole(((UserForAdminRequest) userRequest).getRole()));
        }
        return user;
    }

    public Role mapStringToRole(String role) {
        role = role.toUpperCase();

        switch (role) {
            case "ADMIN" -> {
                return Role.ADMIN;
            }
            case "EMPLOYEE" -> {
                return Role.EMPLOYEE;
            }
            case "USER" -> {
                return Role.USER;
            }
            default -> throw new IllegalArgumentException("Invalid role: " + role);
        }
    }

    public String getBearerUsername() {
        return jwtTokenProvider.getUsername();
    }
    public Role getBearerUserRole() {
        return jwtTokenProvider.getRole();
    }

    // Password validator.
    public boolean isPasswordValid(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        // Password cannot have repeating character only (e.g. 'aaaaaaaa')
        if (password.matches("(.)\\1+")) {
            return false;
        }

        // Password must adhere to the following rules:
        // - Must contain at least one digit
        // - Must contain at least one lowercase character
        // - Must contain at least one uppercase character
        // - Must contain at least one special character
        if (!password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+-={}:;'\",./<>?]).{8,}$")) {
            return false;
        }

        return true;
    }

    private Limits getDefaultLimits() {
        Limits limits = new Limits();
        limits.setDailyTransactionLimit(this.defaultDailyTransactionLimit);
        limits.setTransactionLimit(this.defaultTransactionLimit);
        limits.setAbsoluteLimit(this.defaultAbsoluteLimit);
        return limits;
    }

    public User updateUserForAdmin(int id, UserRequest userForAdminRequest) {
        User user = userRepository.findById(id).orElseThrow(()-> new ObjectNotFoundException(id, "User not found"));

        user.setFirstName(userForAdminRequest.getFirst_name());
        user.setLastName(userForAdminRequest.getLast_name());
        user.setEmail(userForAdminRequest.getEmail());
        user.setBsn(userForAdminRequest.getBsn());
        user.setPhoneNumber(userForAdminRequest.getPhone_number());
        // Convert string of format "yyyy-MM-dd" to LocalDate
        LocalDate dateOfBirth = LocalDate.parse(userForAdminRequest.getBirth_date());
        user.setDateOfBirth(dateOfBirth);
        user.setUsername(userForAdminRequest.getUsername());
        user.setPassword(bCryptPasswordEncoder.encode(userForAdminRequest.getPassword()));
        //user.setRole(mapStringToRole(userForAdminRequest.getRo()));

        return userRepository.save(user);
    }
}
