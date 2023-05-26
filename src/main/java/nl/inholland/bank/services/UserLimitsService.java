package nl.inholland.bank.services;

import nl.inholland.bank.models.Limits;
import nl.inholland.bank.models.Role;
import nl.inholland.bank.models.User;
import nl.inholland.bank.repositories.UserLimitsRepository;
import nl.inholland.bank.repositories.UserRepository;
import nl.inholland.bank.utils.JwtTokenProvider;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;

@Service
public class UserLimitsService {
    private UserLimitsRepository userLimitsRepository;
    private JwtTokenProvider jwtTokenProvider;
    private UserRepository userRepository;

    public UserLimitsService(UserLimitsRepository userLimitsRepository, JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
        this.userLimitsRepository = userLimitsRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    public Limits getUserLimits(int userId) throws AuthenticationException {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Role role = jwtTokenProvider.getRole();

        if (role == Role.USER && !user.getUsername().equals(jwtTokenProvider.getUsername())) {
            throw new AuthenticationException("You are not allowed to view this user's limits");
        }

        Limits limits = userLimitsRepository.findFirstByUserId(userId);

        // Calculate the remaining daily limit
        // TODO: Transactions are not yet implemented. This is a placeholder
        limits.setRemainingDailyTransactionLimit(limits.getDailyTransactionLimit());

        return limits;
    }

    public void updateUserLimits(Limits limits) {
        userLimitsRepository.save(limits);
    }
}
