package nl.inholland.bank.services;

import nl.inholland.bank.models.*;
import nl.inholland.bank.models.dtos.TransactionDTO.WithdrawDepositRequest;
import nl.inholland.bank.models.exceptions.UnauthorizedAccessException;
import nl.inholland.bank.models.exceptions.UserNotTheOwnerOfAccountException;
import nl.inholland.bank.repositories.TransactionRepository;
import org.springframework.stereotype.Service;

import javax.naming.InsufficientResourcesException;
import javax.security.auth.login.AccountNotFoundException;
import java.util.Objects;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final UserService userService;
    private final AccountService accountService;

    public TransactionService(TransactionRepository transactionRepository, UserService userService, AccountService accountService) {
        this.transactionRepository = transactionRepository;
        this.userService = userService;
        this.accountService = accountService;
    }

    public Transaction createTransaction(User user, Account AccountSender, Account AccountReceiver, CurrencyType currencyType, double amount, TransactionType transactionType) {
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setAccountSender(AccountSender);
        transaction.setAccountReceiver(AccountReceiver);
        transaction.setCurrencyType(currencyType);
        transaction.setAmount(amount);
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
            throw new UserNotTheOwnerOfAccountException("User is not the owner of the account");
        }

        if (Objects.equals(userService.getBearerUsername(), performerUserName) || Objects.equals(userService.getBearerUsername(), accountSender.getUser().getUsername())) {
            if (checkAccountExist(accountSender) && accountSender.isActive() && accountSender.getType() != AccountType.SAVING) {
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
        }else {
            throw new UnauthorizedAccessException("You are not authorized to perform this action");
        }
    }

    public Transaction depositMoney(WithdrawDepositRequest depositRequest) throws AccountNotFoundException, UnauthorizedAccessException {
        Account accountReceiver = accountService.getAccountByIban(depositRequest.IBAN());
        User user = userService.getUserById(depositRequest.userId());

        if (!isTransactionNotAuthorizedForUserAccount(user, accountReceiver)) {
            throw new UnauthorizedAccessException("User is not the owner of the account");
        }

        if (checkAccountExist(accountReceiver) && accountReceiver.isActive()) {
            updateAccountBalance(accountReceiver, depositRequest.amount(), true);

            Transaction transaction = mapDepositRequestToTransaction(depositRequest);
            return transactionRepository.save(transaction);
        } else {
            throw new AccountNotFoundException("Account not found or inactive");
        }

    }

    public boolean checkAccountExist(Account account) {
        if (account != null) {
            return true;
        }
        return false;
    }

    public void transferMoney(User user, Account accountSender, Account accountReceiver, double amount, CurrencyType
            currencyType) {
        // Check what account types are
        if (accountSender.getType() == AccountType.SAVING || accountReceiver.getType() == AccountType.SAVING) {
            if (accountSender.getUser() == accountReceiver.getUser()) {
                System.out.println("You can transfer money to your own savings account");
                return;
            }
            System.out.println("You can't transfer money from between these accounts");
            return;
        }
    }

    public boolean checkAccountBalance(Account account, double amount) {
        return account.getBalance() >= amount;
    }

    public void updateAccountBalance(Account account, double amount, boolean isDeposit) {
        // If deposit, add amount to balance, else subtract amount from balance
        if (isDeposit) {
            account.setBalance(account.getBalance() + amount);
        } else {
            account.setBalance(account.getBalance() - amount);
        }

        // TODO: accountService.updateAccount(account);
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
