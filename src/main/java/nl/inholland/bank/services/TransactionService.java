package nl.inholland.bank.services;

import nl.inholland.bank.models.*;
import nl.inholland.bank.models.dtos.TransactionDTO.WithdrawDepositRequest;
import nl.inholland.bank.repositories.TransactionRepository;
import org.springframework.stereotype.Service;

import javax.naming.InsufficientResourcesException;
import javax.security.auth.login.AccountNotFoundException;

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


    public Transaction withdrawMoney(WithdrawDepositRequest withdrawDepositRequest) throws AccountNotFoundException, InsufficientResourcesException {
        try {
            Account accountSender = accountService.getAccountByIban(withdrawDepositRequest.IBAN());
            if (checkAccountExist(accountSender) && accountSender.isActive() && accountSender.getType() != AccountType.SAVING) {
                if (checkAccountBalance(accountSender, withdrawDepositRequest.amount())) {
                    updateAccountBalance(accountSender, withdrawDepositRequest.amount(), false);
                }

                Transaction transaction = mapWithdrawRequestToTransaction(withdrawDepositRequest);
                return transactionRepository.save(transaction);
            } else {
                throw new AccountNotFoundException("Account not found or inactive");
            }
        } catch (Exception e) {
            throw new AccountNotFoundException("Account not found");
        }
    }

    public Transaction depositMoney(WithdrawDepositRequest depositRequest) throws AccountNotFoundException {
        try {
            Account accountReceiver = accountService.getAccountByIban(depositRequest.IBAN());
            if (checkAccountExist(accountReceiver) && accountReceiver.isActive()) {
                updateAccountBalance(accountReceiver, depositRequest.amount(), true);

                Transaction transaction = mapDepositRequestToTransaction(depositRequest);
                return transactionRepository.save(transaction);
            } else {
                throw new AccountNotFoundException("Account not found or inactive");
            }
        } catch (Exception e) {
            throw new AccountNotFoundException("Account not found");
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
