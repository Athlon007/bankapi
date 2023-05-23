package nl.inholland.bank.services;

import nl.inholland.bank.models.Role;
import nl.inholland.bank.models.User;
import nl.inholland.bank.models.dtos.UserForAdminRequest;
import nl.inholland.bank.models.dtos.UserRequest;
import nl.inholland.bank.repositories.UserRepository;
import nl.inholland.bank.utils.JwtTokenProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UserService {
    protected final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public void addUser(UserRequest userRequest) {
        User user = mapUserRequestToUser(userRequest);
        userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return (List<User>)userRepository.findAll();
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
        user.setRoles(List.of(Role.USER));
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
        user.setRoles(List.of(Role.USER));
        return user;
    }

    public List<Role> mapStringArrayToRole(String[] roles) {
        // User may have multiple roles, so we need to check for each role.
        List<Role> roleList = new ArrayList<>();
        for (String role : roles) {
            role = role.toUpperCase();
            switch (role) {
                case "ADMIN":
                    roleList.add(Role.ADMIN);
                    break;
                case "EMPLOYEE":
                    roleList.add(Role.EMPLOYEE);
                    break;
                case "USER":
                    roleList.add(Role.USER);
                    break;
            }
        }
    }
}
