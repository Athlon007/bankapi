package nl.inholland.bank.services;

import nl.inholland.bank.models.*;
import nl.inholland.bank.models.dtos.TransactionDTO.TransactionRequest;
import nl.inholland.bank.models.dtos.TransactionDTO.TransactionSearchRequest;
import nl.inholland.bank.models.dtos.TransactionDTO.WithdrawDepositRequest;
import nl.inholland.bank.models.exceptions.*;
import nl.inholland.bank.repositories.TransactionRepository;
import nl.inholland.bank.repositories.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import javax.naming.InsufficientResourcesException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.sasl.AuthenticationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final AccountService accountService;
    private final UserLimitsService userLimitsService;

    private static final LocalDateTime EARLIEST_TIME = LocalDateTime.of(1, 1, 1, 0, 0, 0);

    /**
     * @param transactionRepository the transaction repository
     * @param userRepository the user repository
     * @param userService the user service
     * @param accountService the account service
     * @param userLimitsService the user limits service
     */
    public TransactionService(TransactionRepository transactionRepository, UserRepository userRepository, UserService userService,
                              AccountService accountService, UserLimitsService userLimitsService) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.accountService = accountService;
        this.userLimitsService = userLimitsService;
    }

    /**
     * @param user the user that is authenticated
     * @param accountSender the account that is sending the money
     * @param accountReceiver the account that is receiving the money
     * @param currencyType the currency type of the transaction
     * @param amount the amount of the transaction
     * @param description the description of the transaction
     * @param transactionType the type of the transaction
     * @return the transaction object
     */
    public Transaction createTransaction(User user, Account accountSender, Account accountReceiver, CurrencyType currencyType, double amount, String description, TransactionType transactionType) {
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setAccountSender(accountSender);
        transaction.setAccountReceiver(accountReceiver);
        transaction.setCurrencyType(currencyType);
        transaction.setAmount(amount);
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setDescription(description);
        transaction.setTransactionType(transactionType);

        return transaction;
    }

    public boolean isTransactionAuthorizedForUserAccount(User user, Account account) {
        return user == account.getUser();
    }

    /**
     * @param withdrawDepositRequest request containing the IBAN of the account and the amount to be deposited
     * @return the transaction object
     * @throws AccountNotFoundException if the account is not found
     * @throws InsufficientResourcesException if the account does not have enough balance
     * @throws AuthenticationException if the user is not authenticated
     * @throws UserNotTheOwnerOfAccountException if the user is not the owner of the account
     */
    public Transaction withdrawMoney(WithdrawDepositRequest withdrawDepositRequest) throws AccountNotFoundException, InsufficientResourcesException, AuthenticationException, UserNotTheOwnerOfAccountException, javax.naming.AuthenticationException {
        Account accountSender = accountService.getAccountByIBAN(withdrawDepositRequest.IBAN());
        User user = getUserByUsername();
        checkAccountPreconditionsForWithdrawOrDeposit(accountSender, user);
        checkUserLimits(accountSender, withdrawDepositRequest.amount());
        updateAccountBalance(accountSender, withdrawDepositRequest.amount(), false);

        Transaction transaction = createTransaction(user, accountSender, null, withdrawDepositRequest.currencyType(), withdrawDepositRequest.amount(), "Withdraw successful", TransactionType.WITHDRAWAL);
        return transactionRepository.save(transaction);
    }

    /**
     * @param depositRequest the request body
     * @return the transaction object
     * @throws AccountNotFoundException          if the account is not found
     * @throws AuthenticationException           if the user is not authenticated
     * @throws UserNotTheOwnerOfAccountException if the user is not the owner of the account
     * @throws InsufficientResourcesException    if the account does not have enough balance
     */
    public Transaction depositMoney(WithdrawDepositRequest depositRequest) throws AccountNotFoundException, AuthenticationException, UserNotTheOwnerOfAccountException, InsufficientResourcesException {
        Account accountReceiver = accountService.getAccountByIBAN(depositRequest.IBAN());
        User user = getUserByUsername();
        checkAccountPreconditionsForWithdrawOrDeposit(accountReceiver, user);
        updateAccountBalance(accountReceiver, depositRequest.amount(), true);
        Transaction transaction = createTransaction(user, null, accountReceiver, depositRequest.currencyType(), depositRequest.amount(), "Deposit successful", TransactionType.DEPOSIT);

        return transactionRepository.save(transaction);
    }

    User getUserByUsername() throws AccountNotFoundException {
        Optional<User> user = userRepository.findUserByUsername(userService.getBearerUsername());
        if (user.isPresent()) {
            return user.get();
        } else {
            throw new AccountNotFoundException("Account not found or inactive");
        }
    }

    private void checkAccountPreconditionsForWithdrawOrDeposit(Account account, User user) throws UserNotTheOwnerOfAccountException {
        if (!isTransactionAuthorizedForUserAccount(user, account)) {
            throw new UserNotTheOwnerOfAccountException("You are not the owner of this account");
        }

        if (!account.isActive()) {
            throw new InactiveAccountException("You cannot deposit/withdraw money to an inactive account");
        }

        if (account.getType() == AccountType.SAVING) {
            throw new OperationNotAllowedException("You cannot deposit/withdraw money to a savings account");
        }
    }

    /**
     * Processes the transaction and checks for requirements.
     *
     * @param request The request given of the attempted transaction information.
     * @return Returns the newly made transaction.
     * @throws AccountNotFoundException          If no account has been found.
     * @throws InsufficientResourcesException    If not enough money is present on the sender account.
     * @throws UserNotTheOwnerOfAccountException If the user is not the owner of the transaction.
     */
    public Transaction processTransaction(TransactionRequest request) throws AccountNotFoundException,
            InsufficientResourcesException,
            UserNotTheOwnerOfAccountException,
            javax.naming.AuthenticationException {
        // Get performing user
        User user = null;
        if (userRepository.findUserByUsername(userService.getBearerUsername()).isPresent()) {
            user = userRepository.findUserByUsername(userService.getBearerUsername()).get();
        }

        // Check if accounts exists and get the corresponding accounts of the given IBANs
        Account accountSender = accountService.getAccountByIBAN(request.sender_iban());
        Account accountReceiver = accountService.getAccountByIBAN(request.receiver_iban());

        if (accountSender == null) {
            throw new AccountNotFoundException("Account with IBAN (" + request.sender_iban() + ") not found.");
        }
        if (accountReceiver == null) {
            throw new AccountNotFoundException("Account with IBAN (" + request.receiver_iban() + ") not found.");
        }

        double amount = request.amount();

        // Perform all requirements checks
        checkUserAuthorization(user, accountSender);
        checkAccountStatus(accountSender, "sender");
        checkAccountStatus(accountReceiver, "receiver");
        checkSameAccount(accountSender, accountReceiver);
        checkSavingAccountOwnership(user, accountSender, accountReceiver);
        checkUserLimits(accountSender, amount);

        // If all requirements have been met, create transaction
        return transferMoney(user, accountSender, accountReceiver, accountSender.getCurrencyType(), amount, request.description());
    }

    /**
     * Checks if user is authorized for the transaction.
     *
     * @param user    The user to check.
     * @param account The account of the user.
     * @throws UserNotTheOwnerOfAccountException Exception thrown when not authorized.
     */
    void checkUserAuthorization(User user, Account account) throws UserNotTheOwnerOfAccountException {
        if (!isUserAuthorizedForTransaction(user, account)) {
            throw new UserNotTheOwnerOfAccountException("You are not authorized to perform this transaction.");
        }
    }

    /**
     * Checks if the user performing the action is the owner of the account OR if the user is an employee.
     *
     * @param user    The user to check their privileges.
     * @param account The account to compare the owner to.
     * @return Returns a boolean if the user is authorized.
     */
    boolean isUserAuthorizedForTransaction(User user, Account account) {
        Role role = userService.getBearerUserRole();
        if (role == Role.USER) {
            return Objects.equals(account.getUser(), user);
        } else return role == Role.EMPLOYEE || role == Role.ADMIN;
    }

    /**
     * Checks if the account is active.
     *
     * @param account     The account to check.
     * @param accountType The account type to message back (Only used for the message).
     * @throws InactiveAccountException Exception if the account is currently inactive.
     */
    void checkAccountStatus(Account account, String accountType) throws InactiveAccountException {
        if (!account.isActive()) {
            throw new InactiveAccountException("The " + accountType + " account is currently inactive and can't transfer money.");
        }
    }

    /**
     * Check if sender and receiver account are the same.
     *
     * @param accountSender   // The sender account to check.
     * @param accountReceiver // The receiver account to check.
     * @throws SameAccountTransferException Exception if both accounts are equal to each other.
     */
    void checkSameAccount(Account accountSender, Account accountReceiver) throws SameAccountTransferException {
        if (Objects.equals(accountSender.getIBAN(), accountReceiver.getIBAN())) {
            throw new SameAccountTransferException("You can't send money to the same account.");
        }
    }

    /**
     * Checks if both accounts belong to the user when an account is of the type SAVING.
     *
     * @param user            The user of the accounts to check.
     * @param accountSender   The sender account to check.
     * @param accountReceiver The receiver account to check.
     * @throws UserNotTheOwnerOfAccountException Exception if the user is not the owner of the account.
     */
    void checkSavingAccountOwnership(User user, Account accountSender, Account accountReceiver) throws UserNotTheOwnerOfAccountException {
        boolean isSavingAccountTransaction = accountSender.getType() == AccountType.SAVING || accountReceiver.getType() == AccountType.SAVING;
        boolean isUserOwner = accountSender.getUser() == user && accountReceiver.getUser() == user;

        if (isSavingAccountTransaction && !isUserOwner) {
            throw new UserNotTheOwnerOfAccountException("For transactions from or to a saving account both the accounts need to belong to you.");
        }
    }

    /**
     * Checks the account holder's limits.
     *
     * @param accountSender The account to check.
     * @param amount        The amount of the transaction.
     * @throws javax.naming.AuthenticationException Exception if user is not authenticated.
     * @throws TransactionLimitException Exception if transaction exceeds limit.
     * @throws DailyTransactionLimitException Exception if transaction exceeds daily limit.
     * @throws InsufficientFundsException Exception if insufficient funds.
     */
    void checkUserLimits(Account accountSender, double amount) throws TransactionLimitException,
            DailyTransactionLimitException, InsufficientFundsException, javax.naming.AuthenticationException {
        Limits limits = this.userLimitsService.getUserLimits(accountSender.getUser().getId());

        if (amount > limits.getTransactionLimit()) {
            throw new TransactionLimitException("Amount exceeds the transaction limit.");
        } else if (amount > limits.getRemainingDailyTransactionLimit()) {
            throw new DailyTransactionLimitException("Amount exceeds remaining daily transaction limit.");
        } else if (accountSender.getBalance() - amount < accountSender.getAbsoluteLimit()) {
            throw new InsufficientFundsException("Insufficient funds, transaction exceeds the absolute limit.");
        }
    }

    /**
     * Creates a new transaction and updates the balances of the sender and receiver.
     *
     * @param user            The user performing the transaction.
     * @param accountSender   The account where the money originates from.
     * @param accountReceiver The account where the money will be deposited.
     * @param currencyType    The type of currency used.
     * @param amount          The amount of money transferred.
     * @param description     The description of the transaction.
     * @return Returns a new transaction.
     */
    public Transaction transferMoney(User user, Account accountSender, Account accountReceiver,
                                     CurrencyType currencyType, double amount, String description) {
        // Create the transaction
        Transaction transaction = createTransaction(user, accountSender, accountReceiver, currencyType, amount,
                description, TransactionType.TRANSACTION);

        // Update the account balances
        updateAccountBalance(accountSender, amount, false);
        updateAccountBalance(accountReceiver, amount, true);

        // Save the transaction
        transactionRepository.save(transaction);

        // Return the transaction
        return transaction;
    }

    /**
     * @param account The account to update.
     * @param amount The amount to update the balance with.
     * @param isDeposit If the amount is a deposit or withdraw (or not).
     */
    public void updateAccountBalance(Account account, double amount, boolean isDeposit) {
        // If deposited, add amount to balance, else subtract amount from balance
        if (isDeposit) {
            account.setBalance(account.getBalance() + amount);
        } else {
            account.setBalance(account.getBalance() - amount);
        }

        accountService.updateAccount(account);
    }

    /**
     * Retrieves transactions dependent on user role and requests
     *
     * @param page    Page of results.
     * @param limit   Limit amount of results.
     * @param request The request to query by.
     * @return Returns a list of Transactions.
     * @throws AuthenticationException If user is not authorized to perform the action.
     */
    public List<Transaction> getTransactions(Optional<Integer> page, Optional<Integer> limit,
                                             TransactionSearchRequest request) throws AuthenticationException {
        // Set up search criteria
        double minAmount = request.minAmount().orElse(0.0);
        double maxAmount = request.maxAmount().orElse(Double.MAX_VALUE);
        LocalDateTime startDateTime = request.startDate().orElse(EARLIEST_TIME);
        LocalDateTime endDateTime = request.endDate().orElse(LocalDateTime.now());
        int transactionID = request.transactionID().orElse(0);
        String ibanSender = request.ibanSender().orElse("");
        String ibanReceiver = request.ibanReceiver().orElse("");
        TransactionType transactionType = null;
        if (request.transactionType().isPresent()) {
            transactionType = mapTransactionTypeToString(request.transactionType().get());
        }

        // Get users by ID
        User userSender = null;
        User userReceiver = null;
        if (request.userSenderId().isPresent()) {
            userSender = userService.getUserById(request.userSenderId().get());
        }
        if (request.userReceiverId().isPresent()) {
            userReceiver = userService.getUserById(request.userReceiverId().get());
        }

        // Set up pagination
        int pageNumber = page.orElse(0);
        int pageSize = limit.orElse(10);
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        // Check user role
        Role userRole = userService.getBearerUserRole();
        if (userRole == null) {
            throw new AuthenticationException("You are not authorized to perform this action.");
        }

        // Get user if they have the Role.USER, safety check for regular users to only see their own transactions.
        User user = null;
        if (userRole == Role.USER && userRepository.findUserByUsername(userService.getBearerUsername()).isPresent()) {
            // Add client info to user, this is added as a check so that users can only see transactions
            // Which they are a part of themselves.
            user = userRepository.findUserByUsername(userService.getBearerUsername()).get();
        }

        return transactionRepository.findTransactions(
                minAmount, maxAmount, startDateTime, endDateTime, transactionID,
                ibanSender, ibanReceiver, user, userSender, userReceiver, transactionType,
                pageable).getContent();
    }

    /**
     * @param transactionType The transaction type to map.
     * @return Returns a TransactionType.
     */
    public TransactionType mapTransactionTypeToString(String transactionType) {
        switch (transactionType.toUpperCase()) {
            case "DEPOSIT":
                return TransactionType.DEPOSIT;
            case "WITHDRAWAL":
                return TransactionType.WITHDRAWAL;
            default:
                return TransactionType.TRANSACTION;
        }
    }
}
