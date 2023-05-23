package nl.inholland.bank.services;

import nl.inholland.bank.models.User;
import nl.inholland.bank.models.dtos.UserRequest;
import nl.inholland.bank.repositories.UserRepository;
import nl.inholland.bank.utils.JwtTokenProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class UserService {
    private UserRepository userRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private JwtTokenProvider jwtTokenProvider;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public void addUser(UserRequest userRequest) {
        User user = new User();
        user.setEmail(userRequest.email());
        user.setUsername(userRequest.username());
        user.setPassword(bCryptPasswordEncoder.encode(userRequest.password()));
        userRepository.save(user);
    }
}
