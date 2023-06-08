package nl.inholland.bank.services;

import nl.inholland.bank.models.*;
import nl.inholland.bank.models.dtos.AuthDTO.LoginRequest;
import nl.inholland.bank.models.dtos.AuthDTO.RefreshTokenRequest;
import nl.inholland.bank.models.dtos.AuthDTO.jwt;
import nl.inholland.bank.models.dtos.Token;
import nl.inholland.bank.models.dtos.UserDTO.UserForAdminRequest;
import nl.inholland.bank.models.dtos.UserDTO.UserRequest;
import nl.inholland.bank.models.exceptions.OperationNotAllowedException;
import nl.inholland.bank.repositories.AccountRepository;
import nl.inholland.bank.repositories.UserRepository;
import nl.inholland.bank.utils.JwtTokenProvider;
import javax.naming.AuthenticationException;

import org.hibernate.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Service class for User objects.
 */
@Service
public class UserService {
    protected final UserRepository userRepository;
    private final UserLimitsService userLimitsService;
    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${bankapi.application.request.limits}")
    private int defaultGetAllUsersLimit;

    public static final String USERNAME_NOT_FOUND = "Username not found.";
    public static final String USER_NOT_FOUND = "User not found.";
    public static final String USERNAME_ALREADY_EXISTS = "Username already exists.";


    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, JwtTokenProvider jwtTokenProvider, UserLimitsService userLimitsService, AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userLimitsService = userLimitsService;
        this.accountRepository = accountRepository;
    }

    /**
     * Adds a new user to the database.
     * @param userRequest User request that will be mapped to a User object.
     * @return User object that was added to the database.
     * @throws AuthenticationException Thrown if UserForAdminRequest is made, but bearer is not an admin.<br>
     */
    public User addUser(UserRequest userRequest) throws AuthenticationException {
        // If current token bearer is not an admin, and userRequest is type of UserForAdminRequest, throw exception.
        if (getBearerUserRole() != Role.ADMIN && userRequest instanceof UserForAdminRequest) {
            throw new AuthenticationException("You are not authorized to create accounts with roles. Remove 'role' from request body.");
        }

        if (userRepository.findUserByUsername(userRequest.getUsername()).isPresent()) {
            throw new IllegalArgumentException(USERNAME_ALREADY_EXISTS);
        }

        if (!isPasswordValid(userRequest.getPassword())) {
            throw new IllegalArgumentException("Password does not meet requirements.");
        }

        if (Boolean.TRUE.equals(userRepository.existsByEmail(userRequest.getEmail()))) {
            throw new IllegalArgumentException("Email already exists.");
        }

        User user = mapUserRequestToUser(userRequest);
        user.setLimits(userLimitsService.getDefaultLimits());
        userRepository.save(user);
        userLimitsService.initialiseLimits(user);
        return userRepository.findUserByUsername(user.getUsername()).orElseThrow(() -> new ObjectNotFoundException(user.getId(), "User"));
    }

    /**
     * Adds a new ADMIN user to the database. Used only once during initial setup of the application.
     * @param userRequest User request that will be mapped to a User object.
     * @return User object that was added to the database.
     */
    public User addAdmin(UserForAdminRequest userRequest) {
        if (userRepository.findUserByUsername(userRequest.getUsername()).isPresent()) {
            throw new IllegalArgumentException(USERNAME_ALREADY_EXISTS);
        }

        if (!isPasswordValid(userRequest.getPassword())) {
            throw new IllegalArgumentException("Password does not meet requirements.");
        }

        User user = mapUserRequestToUser(userRequest);
        user.setLimits(userLimitsService.getDefaultLimits());
        userRepository.save(user);
        userLimitsService.initialiseLimits(user);
        return userRepository.findUserByUsername(user.getUsername()).orElseThrow(() -> new ObjectNotFoundException(user.getId(), "User"));
    }

    /**
     * Returns all users from the database as a list.
     * @param page (Optional. Default: 0) Page number.
     * @param limit (Optional. Default: 50) Limit of users per page.
     * @param name (Optional) Name of user to search for.
     * @param hasNoAccount (Optional) If true, returns only users that have no accounts.
     * @param isActive (Optional) If true, returns only active users.
     * @return List of users.
     */
    public List<User> getAllUsers(Optional<Integer> page, Optional<Integer> limit, Optional<String> name, Optional<Boolean> hasNoAccount, Optional<Boolean> isActive) {
        // If user has role ADMIN, return all users.
        // Otherwise, return only users that have accounts.

        // Limit may be not present, so we need to check for that.
        int pageValue = page.orElse(0);
        int limitValue = limit.orElse(defaultGetAllUsersLimit);

        Role userRole = getBearerUserRole();

        // Declare pageable, so we can limit the results.
        Pageable pageable = PageRequest.of(pageValue, limitValue);

        List<User> users = null;

        if (userRole == Role.ADMIN || userRole == Role.EMPLOYEE) {
            users = userRepository.findUsers(pageable, name, hasNoAccount, isActive).getContent();
        } else {
            users = userRepository.findUsers(pageable, name, Optional.of(false), Optional.of(true)).getContent();
        }

        for (User user : users) {
            user.setLimits(userLimitsService.getUserLimitsNoAuth(user.getId()));
        }

        return users;
    }

    /**
     * Returns a user by id.
     * @param id Id of user to return.
     * @return User object.
     */
    public User getUserById(int id) {
        User user = userRepository.findById(id).orElseThrow(()-> new ObjectNotFoundException(id, USER_NOT_FOUND));
        user.setLimits(userLimitsService.getUserLimitsNoAuth(id));
        return user;
    }

    /**
     * Requests a login token for a user.
     * @param loginRequest Login request containing username and password.
     * @return Token object containing access and expiration date.
     * @throws AuthenticationException Thrown if username or password is incorrect, or username was not found.
     * @throws DisabledException Thrown if user is deactivated.
     */
    public Token login(LoginRequest loginRequest) throws AuthenticationException, DisabledException {
        User user = userRepository.findUserByUsername(loginRequest.username())
                .orElseThrow(() -> new AuthenticationException(USERNAME_NOT_FOUND));

        if (!user.isActive())
            throw new DisabledException("User has been deactivated. Please contact customer support.");

        if (!bCryptPasswordEncoder.matches(loginRequest.password(), user.getPassword()))
            throw new AuthenticationException("Password incorrect");

        return jwtTokenProvider.createToken(user.getUsername(), user.getRole());
    }

    /**
     * Creates a refresh token for a user.
     * @param username Username of user to create refresh token for.
     * @return Refresh token.
     * @throws AuthenticationException Thrown if username was not found.
     */
    public String createRefreshToken(String username) throws AuthenticationException {
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new AuthenticationException(USERNAME_NOT_FOUND));

        return jwtTokenProvider.createRefreshToken(user.getUsername());
    }

    /**
     * Refreshes a token.
     * @param refreshTokenRequest Refresh token request containing refresh token.
     * @return New JWT with access_token and refresh_token.
     * @throws AuthenticationException Thrown if username was not found.
     */
    public jwt refresh(RefreshTokenRequest refreshTokenRequest) throws AuthenticationException {
        // Check if it's not expired
        String username = jwtTokenProvider.refreshTokenUsername(refreshTokenRequest.refresh_token());
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new AuthenticationException(USERNAME_NOT_FOUND));

        if (!user.isActive()) {
            throw new DisabledException("User has been deactivated. Please contact customer support.");
        }

        Token token = jwtTokenProvider.createToken(username, user.getRole());

        return new jwt(token.jwt(), jwtTokenProvider.createRefreshToken(username), user.getId(), token.expiresAt());
    }

    /**
     * Map user request to user object.
     * @param userRequest User request to map.
     * @return User object.
     */
    private User mapUserRequestToUser(UserRequest userRequest) {
        User user = new User();
        user.setFirstName(userRequest.getFirstname());
        user.setLastName(userRequest.getLastname());
        user.setEmail(userRequest.getEmail());
        user.setBsn(userRequest.getBsn());
        user.setPhoneNumber(userRequest.getPhone_number());
        user.setDateOfBirth(convertStringToLocalDate(userRequest.getBirth_date()));
        user.setUsername(userRequest.getUsername());
        user.setPassword(userRequest.getPassword());
        user.setPassword(bCryptPasswordEncoder.encode(userRequest.getPassword()));
        user.setRole(Role.USER);
        if (userRequest instanceof UserForAdminRequest userForAdminRequest) {
            user.setRole(mapStringToRole((userForAdminRequest.getRole())));
        }
        return user;
    }

    /**
     * Maps a string to a role.
     * @param role String to map to role.
     * @return Role object.
     */
    public Role mapStringToRole(String role) {
        try {
            role = role.toUpperCase();
            return Role.valueOf(role);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
    }

    /**
     * Returns the username of the user that is currently logged in.
     * @return Username of user that is currently logged in.
     */
    public String getBearerUsername() {
        return jwtTokenProvider.getUsername();
    }

    /**
     * Returns the role of the user that is currently logged in.
     * @return Role of user that is currently logged in.
     */
    public Role getBearerUserRole() {
        return jwtTokenProvider.getRole();
    }

    /**
     * Checks if a password is valid.
     * @param password Password to check.
     * @return True if password is valid, false if not.
     */
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
        return password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+-={}:;'\",./<>?]).{8,}$");
    }

    /**
     * Updates user with given id.
     * @param id Id of user to update.
     * @param userRequest User request containing new user data.
     * @return Updated user.
     * @throws AuthenticationException Thrown if user is not authorized to update user.
     */
    public User updateUser(int id, UserRequest userRequest) throws AuthenticationException {
        if (userRequest instanceof UserForAdminRequest && getBearerUserRole() != Role.ADMIN) {
            throw new AuthenticationException("You are not authorized to change the role of a user.");
        }

        User user = userRepository.findById(id).orElseThrow(()-> new ObjectNotFoundException(id, USER_NOT_FOUND));

        // If requested username already exists, but is not the username of the user that is being updated, then throw exception.
        if (userRepository.findUserByUsername(userRequest.getUsername()).isPresent() && !user.getUsername().equals(userRequest.getUsername())) {
            throw new IllegalArgumentException(USERNAME_ALREADY_EXISTS);
        }

        // If requested email already exists, but is not the email of the user that is being updated, then throw exception.
        if (
                (Boolean.TRUE.equals(userRepository.existsByEmail(userRequest.getEmail())))
                && (!user.getEmail().equals(userRequest.getEmail()))
        ) {
            throw new IllegalArgumentException("Email already exists.");
        }

        // Users can only update their own account.
        // Employees can update all accounts, except for admins.
        if (
                (getBearerUserRole() == Role.USER && !user.getUsername().equals(getBearerUsername()))
                || (getBearerUserRole() == Role.EMPLOYEE && user.getRole() == Role.ADMIN)
        ) {
            throw new AuthenticationException("You are not authorized to update this user.");
        }

        user.setFirstName(userRequest.getFirstname());
        user.setLastName(userRequest.getLastname());
        user.setEmail(userRequest.getEmail());
        user.setBsn(userRequest.getBsn());
        user.setPhoneNumber(userRequest.getPhone_number());
        user.setDateOfBirth(convertStringToLocalDate(userRequest.getBirth_date()));
        user.setUsername(userRequest.getUsername());
        if (userRequest.getPassword() == null || userRequest.getPassword().length() == 0) {
            // If password is empty, keep the old password.
            user.setPassword(user.getPassword());
        } else {
            // Otherwise update the password.
            if (!isPasswordValid(userRequest.getPassword())) {
                throw new IllegalArgumentException("Password is not valid.");
            }
            user.setPassword(bCryptPasswordEncoder.encode(userRequest.getPassword()));
        }
        if (userRequest instanceof UserForAdminRequest userForAdminRequest) {
            user.setRole(mapStringToRole((userForAdminRequest.getRole())));
        }
        user.setActive(true); // Reactivate user if it was deactivated.
        user.setLimits(userLimitsService.getUserLimitsNoAuth(user.getId()));

        return userRepository.save(user);
    }

    /**
     * Deletes user with given id.
     * @param id Id of user to delete.
     * @throws AuthenticationException Thrown if user is not authorized to delete user.
     */
    public void deleteUser(int id) throws AuthenticationException {
        User user = userRepository.findById(id).orElseThrow(()-> new ObjectNotFoundException(id, USER_NOT_FOUND));

        String currentUserName = getBearerUsername();
        Role currentUserRole = getBearerUserRole();

        if (currentUserRole != Role.ADMIN && user.getRole() == Role.ADMIN) {
            throw new AuthenticationException("You are not authorized to delete admins as non-admin user.");
        }

        // Users can only delete their own account.
        // Employees can delete all accounts, except for admins.
        if (currentUserRole == Role.USER && !user.getUsername().equals(currentUserName)) {
            throw new AuthenticationException("You are not authorized to delete this user.");
        }

        // Check if user has savings or checking accounts.
        // Users with any of these accounts cannot be deleted, but they can be deactivated.
        if (user.getCurrentAccount() != null || user.getSavingAccount() != null) {
            // Take those accounts, and deactivate them too.
            if (user.getCurrentAccount() != null) {
                Account currentAccount = user.getCurrentAccount();
                currentAccount.setActive(false);
                accountRepository.save(currentAccount);
            }

            if (user.getSavingAccount() != null) {
                Account savingAccount = user.getSavingAccount();
                savingAccount.setActive(false);
                accountRepository.save(savingAccount);
            }

            user.setActive(false);
            userRepository.save(user);
            return;
        }

        userRepository.delete(user);
    }

    /**
     * Assigns account to user.
     * @param user User to assign account to.
     * @param account Account to assign to user.
     */
    public void assignAccountToUser(User user, Account account) {
        // Make sure that this account is not assigned to another user already.
        if (account.getUser() != null && account.getUser() != user) {
            throw new OperationNotAllowedException("This account is already assigned to another user.");
        }

        if (account.getType() == AccountType.CURRENT) {
            user.setCurrentAccount(account);
        } else if (account.getType() == AccountType.SAVING) {
            user.setSavingAccount(account);
        } else {
            throw new IllegalArgumentException("Invalid account type.");
        }
        userRepository.save(user);
    }

    /**
     * Get user ID by username
     * @param username username of user
     * @return user ID
     */
    public int getUserIdByUsername(String username) {
        return userRepository.findUserByUsername(username).orElseThrow(()-> new ObjectNotFoundException((Object) username, USER_NOT_FOUND)).getId();
    }

    /**
     * Converts string to LocalDate
     * @param date String of format "yyyy-MM-dd"
     * @return LocalDate
     */
    public LocalDate convertStringToLocalDate(String date) {
        if (date == null) {
            throw new IllegalArgumentException("Birth date is required.");
        }

        if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new IllegalArgumentException("Birth date must be in format yyyy-MM-dd");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(date, formatter);
    }
}
