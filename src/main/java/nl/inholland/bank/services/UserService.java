package nl.inholland.bank.services;

import nl.inholland.bank.models.Role;
import nl.inholland.bank.models.User;
import nl.inholland.bank.models.dtos.*;
import nl.inholland.bank.repositories.UserRepository;
import nl.inholland.bank.utils.JwtTokenProvider;
import javax.naming.AuthenticationException;

import org.hibernate.ObjectNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class UserService {
    protected final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public User addUser(UserRequest userRequest) {
        User user = mapUserRequestToUser(userRequest);
        userRepository.save(user);
        return userRepository.findUserByUsername(user.getUsername()).get();
    }

    public User addUserForAdmin(UserForAdminRequest userForAdminRequest) {
        User user = mapUserForAdminRequestToUser(userForAdminRequest);
        userRepository.save(user);
        return userRepository.findUserByUsername(user.getUsername()).get();
    }

    public List<User> getAllUsers() {
        // If user has role ADMIN, return all users.
        // Otherwise, return only users that have accounts.
        return (List<User>)userRepository.findAll();
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
        user.setFirstName(userRequest.first_name());
        user.setLastName(userRequest.last_name());
        user.setEmail(userRequest.email());
        user.setBsn(userRequest.bsn());
        user.setPhoneNumber(userRequest.phone_number());
        // Convert string of format "yyyy-MM-dd" to LocalDate
        LocalDate dateOfBirth = LocalDate.parse(userRequest.birth_date());
        user.setDateOfBirth(dateOfBirth);
        user.setUsername(userRequest.username());
        user.setPassword(bCryptPasswordEncoder.encode(userRequest.password()));
        user.setRole(Role.USER);
        return user;
    }

    public User mapUserForAdminRequestToUser(UserForAdminRequest userForAdminRequest) {
        User user = new User();
        user.setFirstName(userForAdminRequest.first_name());
        user.setLastName(userForAdminRequest.last_name());
        user.setEmail(userForAdminRequest.email());
        user.setBsn(userForAdminRequest.bsn());
        user.setPhoneNumber(userForAdminRequest.phone_number());
        // Convert string of format "yyyy-MM-dd" to LocalDate
        LocalDate dateOfBirth = LocalDate.parse(userForAdminRequest.birth_date());
        user.setDateOfBirth(dateOfBirth);
        user.setUsername(userForAdminRequest.username());
        user.setPassword(bCryptPasswordEncoder.encode(userForAdminRequest.password()));
        user.setRole(mapStringToRole(userForAdminRequest.role()));
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
}
