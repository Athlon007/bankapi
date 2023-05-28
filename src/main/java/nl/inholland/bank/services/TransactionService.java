package nl.inholland.bank.services;

import nl.inholland.bank.models.*;
import nl.inholland.bank.models.dtos.TransactionDTO.TransactionRequest;
import nl.inholland.bank.models.dtos.TransactionDTO.TransactionResponse;
import nl.inholland.bank.models.dtos.TransactionDTO.TransactionSearchRequest;
import nl.inholland.bank.models.dtos.TransactionDTO.WithdrawDepositRequest;
import nl.inholland.bank.models.exceptions.UnauthorizedAccessException;
import nl.inholland.bank.models.exceptions.UserNotTheOwnerOfAccountException;
import nl.inholland.bank.repositories.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.naming.InsufficientResourcesException;
import javax.security.auth.login.AccountNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final UserService userService;
    private final AccountService accountService;


    public TransactionService(TransactionRepository transactionRepository, UserService userService,
                              AccountService accountService) {
        this.transactionRepository = transactionRepository;
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

    public Transaction withdrawMoney(WithdrawDepositRequest withdrawDepositRequest) throws AccountNotFoundException, InsufficientResourcesException, UnauthorizedAccessException, UserNotTheOwnerOfAccountException {
        Account accountSender = accountService.getAccountByIban(withdrawDepositRequest.IBAN());
        User user = userService.getUserById(withdrawDepositRequest.userId());
        String performerUserName = userService.getBearerUsername();

        // This checks if the user is the owner of the account from the request body
        if (!isTransactionAuthorizedForUserAccount(user, accountSender)) {
            throw new UserNotTheOwnerOfAccountException("You are not the owner of this account or you are not an employee");
        }

        // This checks if the username of the logged-in user is the same as the username of the account owner, or if the logged in user is an employee
        if (Objects.equals(userService.getBearerUsername(), performerUserName) || Objects.equals(userService.getBearerUsername(), accountSender.getUser().getUsername())) {
            if (checkAccountExist(accountSender) && accountSender.isActive() && accountSender.getType() != AccountType.SAVING) {
                checkUserDailyLimitAndTransactionLimit(user, withdrawDepositRequest.amount());
                if (checkAccountBalance(accountSender, withdrawDepositRequest.amount())) {
                    updateAccountBalance(accountSender, withdrawDepositRequest.amount(), false);
                } else {
                    throw new InsufficientResourcesException("Account does not have enough balance");
                }

                Transaction transaction = mapWithdrawRequestToTransaction(withdrawDepositRequest);
                return transactionRepository.save(transaction);
            } else {
                throw new AccountNotFoundException("Account not found or inactive");
            }
        } else {
            throw new UnauthorizedAccessException("You are not authorized to perform this action");
        }
    }

    public Transaction depositMoney(WithdrawDepositRequest depositRequest) throws AccountNotFoundException, UnauthorizedAccessException, UserNotTheOwnerOfAccountException, InsufficientResourcesException {
        Account accountReceiver = accountService.getAccountByIban(depositRequest.IBAN());
        User user = userService.getUserById(depositRequest.userId());
        String performerUserName = userService.getBearerUsername();

        if (!isTransactionAuthorizedForUserAccount(user, accountReceiver)) {
            throw new UserNotTheOwnerOfAccountException("You are not the owner of this account or you are not an employee");
        }

        // This checks if the username of the logged-in user is the same as the username of the account owner, or if the logged in user is an employee
        if (Objects.equals(userService.getBearerUsername(), performerUserName) || Objects.equals(userService.getBearerUsername(), accountReceiver.getUser().getUsername())) {
            checkUserDailyLimitAndTransactionLimit(user, depositRequest.amount());

            if (checkAccountExist(accountReceiver) && accountReceiver.isActive()) {
                updateAccountBalance(accountReceiver, depositRequest.amount(), true);
                Transaction transaction = mapDepositRequestToTransaction(depositRequest);
                return transactionRepository.save(transaction);
            } else {
                throw new AccountNotFoundException("Account not found or inactive");
            }
        } else {
            throw new UnauthorizedAccessException("You are not authorized to perform this action");
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
        if (account != null) {
            return true;
        }
        return false;
    }

    /**
     * Processes the transaction and checks for requirements.
     * @param request The request given of the attempted transaction information.
     * @return Returns the newly made transaction.
     * @throws AccountNotFoundException If no account has been found.
     * @throws InsufficientResourcesException If not enough money is present on the sender account.
     * @throws UnauthorizedAccessException If the user is not authorized to perform the transaction.
     * @throws UserNotTheOwnerOfAccountException If the user is not the owner of the transaction.
     */
    public Transaction processTransaction(TransactionRequest request) throws AccountNotFoundException, InsufficientResourcesException, UnauthorizedAccessException, UserNotTheOwnerOfAccountException
    {
        // Get user
        //User user = new User();
        //STATIC FOR NOW
        User user = userService.getUserById(3);

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

        // If all requirements have been met, create transaction
        return transferMoney(user, accountSender, accountReceiver, CurrencyType.EURO, amount, request.description());
    }

    /**
     * Checks if the user performing the action is the owner of the account OR if the user is an employee.
     * @param user The user to check their privileges.
     * @param account The account to compare the owner to.
     * @return Returns a boolean if the user is authorized.
     */
    private boolean isUserAuthorizedForTransaction(User user, Account account)
    {
        if (userService.getBearerUserRole() == Role.USER) {
            return Objects.equals(account.getUser(), user);
        } else return userService.getBearerUserRole() == Role.EMPLOYEE;
    }

    /**
     * Creates a new transaction and updates the balances of the sender and receiver.
     * @param user The user performing the transaction.
     * @param accountSender The account where the money originates from.
     * @param accountReceiver The account where the money will be deposited.
     * @param currencyType The type of currency used.
     * @param amount The amount of money transferred.
     * @param description The description of the transaction.
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

    public Transaction mapWithdrawRequestToTransaction(WithdrawDepositRequest withdrawDepositRequest) {
        Transaction transaction = new Transaction();
        transaction.setAccountSender(accountService.getAccountByIban(withdrawDepositRequest.IBAN()));
        transaction.setAmount(withdrawDepositRequest.amount());
        transaction.setCurrencyType(CurrencyType.EURO);
        User user = userService.getUserById(withdrawDepositRequest.userId());
        transaction.setUser(user);
        transaction.setTransactionType(TransactionType.WITHDRAWAL);
        return transaction;
    }

    private Transaction mapDepositRequestToTransaction(WithdrawDepositRequest depositRequest) {
        Transaction transaction = new Transaction();
        transaction.setAccountReceiver(accountService.getAccountByIban(depositRequest.IBAN()));
        transaction.setAmount(depositRequest.amount());
        transaction.setCurrencyType(CurrencyType.EURO);
        User user = userService.getUserById(depositRequest.userId());
        transaction.setUser(user);
        transaction.setTransactionType(TransactionType.DEPOSIT);

        return transaction;
    }

    public List<Transaction> getTransactions(Optional<Integer> page, Optional<Integer> limit,
                                             TransactionSearchRequest request) {
        int pageNumber = page.orElse(0);
        int pageSize = limit.orElse(10);
        double min = request.minAmount().orElse(0.0);
        double max = request.maxAmount().orElse(Double.MAX_VALUE);
        LocalDateTime start = request.startDate().orElse(LocalDateTime.MIN);
        LocalDateTime end = request.endDate().orElse(LocalDateTime.now());
        String sender = request.ibanSender().orElse("");
        String receiver = request.ibanReceiver().orElse("");

        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        // Retrieves all (If request has no values given)
        if (request.minAmount().isEmpty() && request.maxAmount().isEmpty() &&
                request.startDate().isEmpty() && request.endDate().isEmpty() &&
                request.ibanSender().isEmpty() && request.ibanReceiver().isEmpty()) {
           return transactionRepository.findAll(pageable).getContent();
        }

        //return transactionRepository.findAllByAmountBetweenAndTimestampBetween(min, max, start, end, pageable).getContent();

        return transactionRepository.findAllByAmountBetweenAndTimestampBetweenAndAccountSender_IBANAndAccountReceiver_IBAN(
                min, max, start, end, sender, receiver, pageable).getContent();
    }
}
