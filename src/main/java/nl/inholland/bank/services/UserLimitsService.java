package nl.inholland.bank.services;

import nl.inholland.bank.models.*;
import nl.inholland.bank.models.dtos.UserDTO.UserLimitsRequest;
import nl.inholland.bank.repositories.TransactionRepository;
import nl.inholland.bank.repositories.UserLimitsRepository;
import nl.inholland.bank.repositories.UserRepository;
import nl.inholland.bank.utils.JwtTokenProvider;
import org.hibernate.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.MethodNotAllowedException;

import javax.naming.AuthenticationException;
import java.time.LocalDate;
import java.util.List;

/**
 * Service for managing user limits.
 */
@Service
public class UserLimitsService {
    private final UserLimitsRepository userLimitsRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    @Value("${bankapi.user.defaults.dailyTransactionLimit}")
    private int defaultDailyTransactionLimit;
    @Value("${bankapi.user.defaults.transactionLimit}")
    private int defaultTransactionLimit;

    public UserLimitsService(UserLimitsRepository userLimitsRepository, JwtTokenProvider jwtTokenProvider, UserRepository userRepository, TransactionRepository transactionRepository) {
        this.userLimitsRepository = userLimitsRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Get the limits for a user. Used by internal services.
     * @param userId The id of the user.
     * @return The limits for the user.
     */
    public Limits getUserLimitsNoAuth(int userId) {
        if (userLimitsRepository.findFirstByUserId(userId) == null) {
            throw new ObjectNotFoundException(userId, UserService.USER_NOT_FOUND);
        }
        Limits limits = userLimitsRepository.findFirstByUserId(userId);

        // Get all transactions from today for this user using findAllByTimestampIsAfter.
        List<Transaction> todayTransactions = transactionRepository.findAllByTimestampIsAfterAndUserId(LocalDate.now().atStartOfDay(), userId);
        double remainingDailyLimit = calculateRemainingDailyLimit(limits, todayTransactions, userId);

        // Calculate the remaining daily limit
        limits.setRemainingDailyTransactionLimit(remainingDailyLimit);

        return limits;
    }

    /**
     * Get the limits for a user. Used by the UserController.
     * @param userId The id of the user.
     * @return The limits for the user.
     * @throws AuthenticationException If the user is not allowed to view the limits.
     */
    public Limits getUserLimits(int userId) throws AuthenticationException {
        User user = userRepository.findById(userId).orElseThrow(() -> new ObjectNotFoundException(userId, UserService.USERNAME_NOT_FOUND));
        Role role = jwtTokenProvider.getRole();

        if (role == Role.CUSTOMER && !user.getUsername().equals(jwtTokenProvider.getUsername())) {
            throw new AuthenticationException("You are not allowed to view this user's limits");
        }

        // If user has no currents account or savings account, throw an exception.
        // It does not make sense to request limits for a user with no accounts (ex. some employees).
        if (user.getCurrentAccount() == null && user.getSavingAccount() == null) {
            throw new MethodNotAllowedException("User has no accounts", null);
        }

        return getUserLimitsNoAuth(userId);
    }
    /**
     * Initialise the limits for a new user.
     * @param user The user to initialise the limits for.
     */
    protected void initialiseLimits(User user) {
        Limits limits = getDefaultLimits();
        limits.setUser(user);
        userLimitsRepository.save(limits);
    }

    /**
     * Update the limits for a user.
     * @param userId The id of the user.
     * @param userLimitsRequest  The limits to update.
     * @return The updated limits.
     * @throws AuthenticationException If the user is not allowed to update the limits.
     */
    public Limits updateUserLimits(int userId, UserLimitsRequest userLimitsRequest) throws  AuthenticationException {
        // Users cannot update limits: theirs or others.
        if (jwtTokenProvider.getRole() == Role.CUSTOMER) {
            throw new AuthenticationException("You are not allowed to update limits");
        }

        userRepository.findById(userId).orElseThrow(() -> new ObjectNotFoundException(userId, UserService.USER_NOT_FOUND));
        Limits limits = userLimitsRepository.findFirstByUserId(userId);
        limits.setTransactionLimit(userLimitsRequest.transaction_limit());
        limits.setDailyTransactionLimit(userLimitsRequest.daily_transaction_limit());

        userLimitsRepository.save(limits);

        Limits limitsResponse = userLimitsRepository.findFirstByUserId(userId);
        // Get all transactions from today for this user using findAllByTimestampIsAfter.
        List<Transaction> todayTransactions = transactionRepository.findAllByTimestampIsAfterAndUserId(LocalDate.now().atStartOfDay(), userId);
        double remainingDailyLimit = calculateRemainingDailyLimit(limits, todayTransactions, userId);

        // Calculate the remaining daily limit
        limitsResponse.setRemainingDailyTransactionLimit(remainingDailyLimit);

        return limitsResponse;
    }

    /**
     * Calculate the remaining daily limit for a user.
     * @param limits The limits for the user.
     * @param todaysTransactions The transactions for the user today.
     * @param userId The id of the user.
     * @return The remaining daily limit.
     */
    public Double calculateRemainingDailyLimit(Limits limits, List<Transaction> todaysTransactions, int userId) {
        double totalToday = 0;
        for (Transaction transaction : todaysTransactions) {
            // Ignore transactions from SAVINGS accounts.
            if (transaction.getAccountSender() != null && transaction.getAccountSender().getType().equals(AccountType.SAVING)) {
                continue;
            }

            // Ignore transactions to SAVINGS accounts.
            if (transaction.getAccountReceiver() != null && transaction.getAccountReceiver().getType().equals(AccountType.SAVING)) {
                continue;
            }

            // Ignore deposits to CURRENT accounts.
            if (transaction.getAccountSender() == null && transaction.getAccountReceiver() != null) {
                continue;
            }

            // Ignore deposits to CURRENT accounts.
            if (transaction.getAccountReceiver() != null && transaction.getAccountSender().getUser().getId() != userId) {
                  continue;
            }

            totalToday += transaction.getAmount();
        }

        return limits.getDailyTransactionLimit() - totalToday;
    }

    /**
     * Get the default limits for a user.
     * @return The default limits.
     */
    public Limits getDefaultLimits() {
        Limits limits = new Limits();
        limits.setDailyTransactionLimit(this.defaultDailyTransactionLimit);
        limits.setTransactionLimit(this.defaultTransactionLimit);
        limits.setRemainingDailyTransactionLimit(this.defaultDailyTransactionLimit);
        return limits;
    }
}
