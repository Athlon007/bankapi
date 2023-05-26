package nl.inholland.bank.services;

import nl.inholland.bank.models.Limits;
import nl.inholland.bank.models.Role;
import nl.inholland.bank.models.User;
import nl.inholland.bank.models.dtos.UserDTO.UserLimitsRequest;
import nl.inholland.bank.repositories.UserLimitsRepository;
import nl.inholland.bank.repositories.UserRepository;
import nl.inholland.bank.utils.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;

@Service
public class UserLimitsService {
    private UserLimitsRepository userLimitsRepository;
    private JwtTokenProvider jwtTokenProvider;
    private UserRepository userRepository;

    @Value("${bankapi.user.defaults.dailyTransactionLimit}")
    private int defaultDailyTransactionLimit;
    @Value("${bankapi.user.defaults.transactionLimit}")
    private int defaultTransactionLimit;
    @Value("${bankapi.user.defaults.absoluteLimit}")
    private int defaultAbsoluteLimit;

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
        limits.setRemainingDailyTransactionLimit(calculateRemainingDailyLimit(limits));

        return limits;
    }

    // Used to initialise the limits for a new user
    protected void initialiseLimits(User user) {
        Limits limits = getDefaultLimits();
        limits.setUser(user);
        userLimitsRepository.save(limits);
    }

    public void updateUserLimits(int userId, UserLimitsRequest userLimitsRequest) throws  AuthenticationException {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        // Users cannot update limits: theirs or others.
        if (jwtTokenProvider.getRole() == Role.USER) {
            throw new AuthenticationException("You are not allowed to update limits");
        }

        Limits limits = mapUserLimitsRequestToLimits(userLimitsRequest);
        userLimitsRepository.save(limits);
    }

    private Limits mapUserLimitsRequestToLimits(UserLimitsRequest userLimitsRequest) {
        Limits limits = new Limits();
        limits.setTransactionLimit(userLimitsRequest.transaction_limit());
        limits.setDailyTransactionLimit(userLimitsRequest.daily_transaction_limit());
        limits.setAbsoluteLimit(userLimitsRequest.absolute_limit());
        limits.setRemainingDailyTransactionLimit(calculateRemainingDailyLimit(limits));
        return limits;
    }

    private Double calculateRemainingDailyLimit(Limits limits) {
        // TODO: Transactions are not yet implemented. This is a placeholder
        return limits.getDailyTransactionLimit();
    }

    public Limits getDefaultLimits() {
        Limits limits = new Limits();
        limits.setDailyTransactionLimit(this.defaultDailyTransactionLimit);
        limits.setTransactionLimit(this.defaultTransactionLimit);
        limits.setAbsoluteLimit(this.defaultAbsoluteLimit);
        return limits;
    }
}
