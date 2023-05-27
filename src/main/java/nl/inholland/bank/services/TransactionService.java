package nl.inholland.bank.services;

import nl.inholland.bank.models.*;
import nl.inholland.bank.models.dtos.TransactionDTO.WithdrawRequest;
import nl.inholland.bank.repositories.AccountRepository;
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

    public Transaction withdrawMoney(WithdrawRequest withdrawRequest) throws AccountNotFoundException, InsufficientResourcesException {
        Account accountSender = accountService.getAccountByIban(withdrawRequest.IBAN());
        try {
            if (checkAccountExist(accountSender) && accountSender.isActive()) {
                if (checkAccountBalance(accountSender, withdrawRequest.amount())) {
                    updateAccountBalance(accountSender, withdrawRequest.amount(), false);
                }

                Transaction transaction = mapWithdrawRequestToTransaction(withdrawRequest);
                return transactionRepository.save(transaction);
            }
            else {
                throw new AccountNotFoundException("Account not found or inactive");
            }
        } catch (Exception e) {
            throw new AccountNotFoundException("Account not found");
        }
    }

//    public void depositMoney(Account account, double amount) {
//        if (checkAccountExist(account)) {
//            return;
//        }
//
//        if (checkAccountBalance(account, amount)) {
//            updateAccountBalance(account, amount, true);
//            Transaction transaction = createTransaction(account.getUser(), null, account, account.getCurrencyType(), amount);
//        }
//    }

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

    public Transaction mapWithdrawRequestToTransaction(WithdrawRequest withdrawRequest) {
        Transaction transaction = new Transaction();
        transaction.setAccountSender(accountService.getAccountByIban(withdrawRequest.IBAN()));
        transaction.setAmount(withdrawRequest.amount());
        transaction.setCurrencyType(CurrencyType.EURO);
        User user = userService.getUserById(withdrawRequest.userId());
        transaction.setUser(user);
        transaction.setTransactionType(TransactionType.WITHDRAWAL);
        return transaction;
    }


}
