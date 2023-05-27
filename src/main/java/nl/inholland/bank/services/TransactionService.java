package nl.inholland.bank.services;

import nl.inholland.bank.models.*;
import nl.inholland.bank.models.dtos.TransactionDTO.WithdrawDepositRequest;
import nl.inholland.bank.models.exceptions.UnauthorizedAccessException;
import nl.inholland.bank.models.exceptions.UserNotTheOwnerOfAccountException;
import nl.inholland.bank.repositories.TransactionRepository;
import org.springframework.stereotype.Service;

import javax.naming.InsufficientResourcesException;
import javax.security.auth.login.AccountNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

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

    public Transaction createTransaction(User user, Account AccountSender, Account AccountReceiver, CurrencyType currencyType, double amount, String description, TransactionType transactionType) {
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setAccountSender(accountSender);
        transaction.setAccountReceiver(accountReceiver);
        transaction.setCurrencyType(currencyType);
        transaction.setAmount(amount);
        transaction.setTimestamp(LocalDate.now());
        transaction.setDescription(description);
        transaction.setTransactionType(transactionType);

        return transaction;
    }

    public boolean isTransactionNotAuthorizedForUserAccount(User user, Account account) {
        return user == account.getUser();
    }

    public Transaction withdrawMoney(WithdrawDepositRequest withdrawDepositRequest) throws AccountNotFoundException, InsufficientResourcesException, UnauthorizedAccessException, UserNotTheOwnerOfAccountException {
        Account accountSender = accountService.getAccountByIban(withdrawDepositRequest.IBAN());
        User user = userService.getUserById(withdrawDepositRequest.userId());
        String performerUserName = userService.getBearerUsername();

        // This checks if the user is the owner of the account from the request body
        if (!isTransactionNotAuthorizedForUserAccount(user, accountSender)) {
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

        if (!isTransactionNotAuthorizedForUserAccount(user, accountReceiver)) {
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

    public Transaction transferMoney(User user, Account accountSender, Account accountReceiver,
                                     CurrencyType currencyType, double amount, String description) {
        // If any account is a saving account...
        if (accountSender.getType() == AccountType.SAVING || accountReceiver.getType() == AccountType.SAVING) {
            // Check if both the sender and receiver account belong to the user performing the transaction.
            if (accountSender.getUser() == accountReceiver.getUser()) {
                return createTransaction(user, accountSender, accountReceiver, currencyType, amount, description);
            } else {
                throw new IllegalArgumentException("You can't transfer from/to a saving account that doesn't belong to you.");
            }
        } else { // Proceed with transaction, check if the user (sender) has enough funds
            if ((accountSender.getBalance() - amount) >= user.getLimits().getAbsoluteLimit()) {
                return createTransaction(user, accountSender, accountReceiver, currencyType, amount, description);
            } else {
                throw new IllegalArgumentException("Insufficient funds to proceed with the transaction.");
            }
        }
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
}
