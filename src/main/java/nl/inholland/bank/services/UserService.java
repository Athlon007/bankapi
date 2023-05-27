package nl.inholland.bank.services;

import nl.inholland.bank.models.*;
import nl.inholland.bank.models.dtos.AuthDTO.LoginRequest;
import nl.inholland.bank.models.dtos.AuthDTO.RefreshTokenRequest;
import nl.inholland.bank.models.dtos.AuthDTO.jwt;
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
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    protected final UserRepository userRepository;
    private final UserLimitsService userLimitsService;
    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${bankapi.application.request.limits}")
    private int defaultGetAllUsersLimit;



    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, JwtTokenProvider jwtTokenProvider, UserLimitsService userLimitsService, AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userLimitsService = userLimitsService;
        this.accountRepository = accountRepository;
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
        user.setLimits(userLimitsService.getDefaultLimits());
        userRepository.save(user);
        userLimitsService.initialiseLimits(user);
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
        user.setLimits(userLimitsService.getDefaultLimits());
        userRepository.save(user);
        userLimitsService.initialiseLimits(user);
        return userRepository.findUserByUsername(user.getUsername()).orElseThrow(() -> new ObjectNotFoundException(user.getId(), "User"));
    }

    public List<User> getAllUsers(Optional<Integer> page, Optional<Integer> limit, Optional<String> name) {
        // If user has role ADMIN, return all users.
        // Otherwise, return only users that have accounts.

        // Limit may be not present, so we need to check for that.
        int pageValue = page.orElse(0);
        int limitValue = limit.orElse(defaultGetAllUsersLimit);

        Role userRole = getBearerUserRole();

        // Declare pageable, so we can limit the results.
        Pageable pageable = PageRequest.of(pageValue, limitValue);

        // TODO: Calculate remaining limits for today.

        if (userRole == Role.ADMIN || userRole == Role.EMPLOYEE) {
            return name.map(
                    s -> userRepository.findAllByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(s, s, pageable).getContent())
                    .orElseGet(() -> userRepository.findAll(pageable).getContent()
                    );
        }

        return name.map(
                s -> userRepository.findAllByCurrentAccountIsNotNullAndActiveIsTrueAndFirstNameContainingIgnoreCaseOrCurrentAccountIsNotNullAndActiveIsTrueAndLastNameContainingIgnoreCase(name.get(), name.get(), pageable).getContent())
                .orElseGet(() -> userRepository.findAllByCurrentAccountIsNotNullAndActiveIsTrue(pageable).getContent()
                );
    }

    public List<User> getAllUsersWithNoAccounts(Optional<Integer> page, Optional<Integer> limit, Optional<String> name) {
        // Users cannot see other users without accounts anyway.
        // Might as well return empty array.
        if (getBearerUserRole() == Role.USER) {
            return List.of();
        }

        Pageable pageable = PageRequest.of(page.orElse(0), limit.orElse(defaultGetAllUsersLimit));

        // We're only checking if current account is null,
        // because user cannot have saving account without current account anyway.
        return name.map(
                s -> userRepository.findAllByCurrentAccountIsNullAndFirstNameContainingIgnoreCaseOrCurrentAccountIsNullAndLastNameContainingIgnoreCase(name.get(), name.get(), pageable))
                .orElseGet(() -> userRepository.findAllByCurrentAccountIsNull(pageable)).getContent();
    }

    public User getUserById(int id) {
        return userRepository.findById(id).orElseThrow(()-> new ObjectNotFoundException(id, "User not found"));
    }

    public String login(LoginRequest loginRequest) throws AuthenticationException, DisabledException {
        User user = userRepository.findUserByUsername(loginRequest.username())
                .orElseThrow(() -> new AuthenticationException("Username not found"));

        if (!user.isActive())
            throw new DisabledException("User has been deactivated. Please contact customer support.");

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

        if (!user.isActive()) {
            throw new DisabledException("User has been deactivated. Please contact customer support.");
        }

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
        if (userRequest instanceof UserForAdminRequest userForAdminRequest) {
            user.setRole(mapStringToRole((userForAdminRequest.getRole())));
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
        return password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+-={}:;'\",./<>?]).{8,}$");
    }



    public User updateUser(int id, UserRequest userRequest) throws AuthenticationException {
        if (userRequest instanceof UserForAdminRequest && getBearerUserRole() != Role.ADMIN) {
            throw new AuthenticationException("You are not authorized to change the role of a user.");
        }

        User user = userRepository.findById(id).orElseThrow(()-> new ObjectNotFoundException(id, "User not found"));

        String currentUserName = getBearerUsername();
        Role currentUserRole = getBearerUserRole();

        // Users can only update their own account.
        // Employees can update all accounts, except for admins.
        if (
                currentUserRole == Role.USER && !user.getUsername().equals(currentUserName)
                || currentUserRole == Role.EMPLOYEE && user.getRole() == Role.ADMIN
        ) {
            throw new AuthenticationException("You are not authorized to update this user.");
        }

        user.setFirstName(userRequest.getFirst_name());
        user.setLastName(userRequest.getLast_name());
        user.setEmail(userRequest.getEmail());
        user.setBsn(userRequest.getBsn());
        user.setPhoneNumber(userRequest.getPhone_number());
        // Convert string of format "yyyy-MM-dd" to LocalDate
        if (userRequest.getBirth_date() == null) {
            throw new IllegalArgumentException("Birth date is required.");
        }
        user.setDateOfBirth(LocalDate.parse(userRequest.getBirth_date()));
        user.setUsername(userRequest.getUsername());
        if (!isPasswordValid(userRequest.getPassword())) {
            throw new IllegalArgumentException("Password is not valid.");
        }
        user.setPassword(bCryptPasswordEncoder.encode(userRequest.getPassword()));
        if (userRequest instanceof UserForAdminRequest userForAdminRequest) {
            if (getBearerUserRole() != Role.ADMIN) {
                throw new AuthenticationException("You are not authorized to change the role of a user.");
            }
            user.setRole(mapStringToRole((userForAdminRequest.getRole())));
        }
        user.setActive(true); // Reactivate user if it was deactivated.

        return userRepository.save(user);
    }

    public void deleteUser(int id) throws AuthenticationException, OperationNotAllowedException {
        User user = userRepository.findById(id).orElseThrow(()-> new ObjectNotFoundException(id, "User not found"));

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

        String currentUserName = getBearerUsername();
        Role currentUserRole = getBearerUserRole();

        // Users can only delete their own account.
        // Employees can delete all accounts, except for admins.
        if (
                currentUserRole == Role.USER && !user.getUsername().equals(currentUserName)
                || currentUserRole == Role.EMPLOYEE && user.getRole() == Role.ADMIN
        ) {
            throw new AuthenticationException("You are not authorized to delete this user.");
        }

        userRepository.delete(user);
    }

    public void assignAccountToUser(User user, Account account) {
        if (account.getType() == AccountType.CURRENT) {
            user.setCurrentAccount(account);
        } else if (account.getType() == AccountType.SAVING) {
            user.setSavingAccount(account);
        } else {
            throw new IllegalArgumentException("Invalid account type.");
        }
        userRepository.save(user);
    }
}
