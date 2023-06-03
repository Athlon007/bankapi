package nl.inholland.bank.services;

import nl.inholland.bank.models.*;
import nl.inholland.bank.models.dtos.TransactionDTO.TransactionRequest;
import nl.inholland.bank.models.dtos.TransactionDTO.TransactionSearchRequest;
import nl.inholland.bank.models.dtos.TransactionDTO.WithdrawDepositRequest;
import nl.inholland.bank.models.exceptions.AccountIsNotActiveException;
import nl.inholland.bank.models.exceptions.UserNotTheOwnerOfAccountException;
import nl.inholland.bank.repositories.TransactionRepository;
import nl.inholland.bank.repositories.UserRepository;
import org.hibernate.ObjectNotFoundException;
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

    private static final LocalDateTime EARLIEST_TIME = LocalDateTime.of(1, 1, 1, 0, 0, 0);

    public TransactionService(TransactionRepository transactionRepository, UserRepository userRepository, UserService userService,
                              AccountService accountService) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.accountService = accountService;
    }

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

    public Transaction withdrawMoney(WithdrawDepositRequest withdrawDepositRequest) throws AccountNotFoundException, InsufficientResourcesException, AuthenticationException, UserNotTheOwnerOfAccountException {
        Account accountSender = accountService.getAccountByIban(withdrawDepositRequest.IBAN());
        User user = null;
        if (userRepository.findUserByUsername(userService.getBearerUsername()).isPresent()) {
            user = userRepository.findUserByUsername(userService.getBearerUsername()).get();
        }
        String performerUserName = userService.getBearerUsername();

        // This checks if the user is the owner of the account from the request body
        if (!isTransactionAuthorizedForUserAccount(user, accountSender)) {
            throw new AuthenticationException("You are not the owner of this account or you are not an employee");
        }

        // check account type
        if (accountSender.getType() == AccountType.SAVING) {
            throw new IllegalArgumentException("You cannot withdraw money from a savings account");
        }

        if (!accountSender.isActive()){
            throw new IllegalArgumentException("You cannot withdraw money from an inactive account");
        }

        // This checks if the username of the logged-in user is the same as the username of the account owner, or if the logged in user is an employee
        if (Objects.equals(userService.getBearerUsername(), performerUserName) || Objects.equals(userService.getBearerUsername(), accountSender.getUser().getUsername())) {
            checkUserDailyLimitAndTransactionLimit(user, withdrawDepositRequest.amount());
            if (checkAccountBalance(accountSender, withdrawDepositRequest.amount())) {
                updateAccountBalance(accountSender, withdrawDepositRequest.amount(), false);
            } else {
                throw new InsufficientResourcesException("Account does not have enough balance");
            }

            Transaction transaction = createTransaction(user, accountSender, null, withdrawDepositRequest.currencyType(), withdrawDepositRequest.amount(), "Withdraw successful", TransactionType.WITHDRAWAL);
            return transactionRepository.save(transaction);
        } else {
            throw new AuthenticationException("You are not authorized to perform this action");
        }
    }

    public Transaction depositMoney(WithdrawDepositRequest depositRequest) throws AccountNotFoundException, AuthenticationException, UserNotTheOwnerOfAccountException, InsufficientResourcesException {
        Account accountReceiver = accountService.getAccountByIban(depositRequest.IBAN());
        User user = null;
        if (userRepository.findUserByUsername(userService.getBearerUsername()).isPresent()) {
            user = userRepository.findUserByUsername(userService.getBearerUsername()).get();
        }
        String performerUserName = userService.getBearerUsername();

        if (!isTransactionAuthorizedForUserAccount(user, accountReceiver)) {
            throw new UserNotTheOwnerOfAccountException("You are not the owner of this account or you are not an employee");
        }

        if (!accountReceiver.isActive()){
            throw new IllegalArgumentException("You cannot deposit money to an inactive account");
        }

        if (accountReceiver.getType() == AccountType.SAVING) {
            throw new IllegalArgumentException("You cannot deposit money to a savings account");
        }

        // This checks if the username of the logged-in user is the same as the username of the account owner, or if the logged in user is an employee
        if (Objects.equals(userService.getBearerUsername(), performerUserName) || Objects.equals(userService.getBearerUsername(), accountReceiver.getUser().getUsername())) {
            checkUserDailyLimitAndTransactionLimit(user, depositRequest.amount());
            if (accountReceiver.isActive()) {
                updateAccountBalance(accountReceiver, depositRequest.amount(), true);
                Transaction transaction = createTransaction(user, null, accountReceiver, depositRequest.currencyType(), depositRequest.amount(), "Deposit successful", TransactionType.DEPOSIT);
                return transactionRepository.save(transaction);
            } else {
                throw new AccountNotFoundException("Account not found or inactive");
            }
        } else {
            throw new AccountNotFoundException("Account not found or inactive");
        }
    }

    public void checkUserDailyLimitAndTransactionLimit(User user, double amount) throws InsufficientResourcesException {
        if (user.getLimits().getDailyTransactionLimit() < amount) {
            throw new InsufficientResourcesException("You have exceeded your daily limit");
        }
        if (user.getLimits().getTransactionLimit() < amount) {
            throw new InsufficientResourcesException("You have exceeded your transaction limit");
        }
    }

    public boolean checkAccountExist(Account account) {
        return account != null;
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
    public Transaction processTransaction(TransactionRequest request) throws AccountNotFoundException, InsufficientResourcesException, UserNotTheOwnerOfAccountException {
        // Get user
        User user = null;
        if (userRepository.findUserByUsername(userService.getBearerUsername()).isPresent()) {
            user = userRepository.findUserByUsername(userService.getBearerUsername()).get();
        }

        // Check if accounts exists and get the corresponding accounts of the given IBANs
        Account accountSender = accountService.getAccountByIBAN(request.sender_iban());
        Account accountReceiver = accountService.getAccountByIBAN(request.receiver_iban());
        double amount = request.amount();

        // Check all requirements
        if (!isUserAuthorizedForTransaction(user, accountSender)) {
            throw new UserNotTheOwnerOfAccountException("You are not authorized to perform this transaction.");
        } else if (!accountSender.isActive()) {
            throw new IllegalArgumentException("The sender account is currently inactive and can't transfer money.");
        } else if (!accountReceiver.isActive()) {
            throw new IllegalArgumentException("The receiver account is currently inactive and can't receive money.");
        } else if (Objects.equals(accountSender.getIBAN(), accountReceiver.getIBAN())) {
            throw new IllegalArgumentException("You can't send money to the same account.");
        } else if (accountSender.getType() == AccountType.SAVING || accountReceiver.getType() == AccountType.SAVING) {
            if (accountSender.getUser() != user || accountReceiver.getUser() != user) {
                throw new UserNotTheOwnerOfAccountException("For a transaction from/to a saving account, " +
                        "both accounts need to belong to you.");
            }
        } else if (!checkAccountBalance(accountSender, amount)) {
            throw new InsufficientResourcesException("Insufficient funds to create the transaction.");
        }

        // TODO : Check if value of transactions that day >= daily transaction value limit
        // TODO : Check if Transaction Value > Transaction Limit
        // TODO : Check for currency type and map the string to CurrencyType

        // If all requirements have been met, create transaction
        return transferMoney(user, accountSender, accountReceiver, CurrencyType.EURO, amount, request.description());
    }

    /**
     * Checks if the user performing the action is the owner of the account OR if the user is an employee.
     *
     * @param user    The user to check their privileges.
     * @param account The account to compare the owner to.
     * @return Returns a boolean if the user is authorized.
     */
    private boolean isUserAuthorizedForTransaction(User user, Account account) {
        if (userService.getBearerUserRole() == Role.USER) {
            return Objects.equals(account.getUser(), user);
        } else return userService.getBearerUserRole() == Role.EMPLOYEE;
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

    public boolean checkAccountBalance(Account account, double amount) {
        return account.getBalance() >= amount;
    }

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
        String ibanSender = request.ibanSender().orElse("");
        String ibanReceiver = request.ibanReceiver().orElse("");

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
                minAmount, maxAmount, startDateTime, endDateTime,
                ibanSender, ibanReceiver, user, userSender, userReceiver,
                pageable).getContent();
    }
}
