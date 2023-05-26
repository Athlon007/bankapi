package nl.inholland.bank.services;

import nl.inholland.bank.models.*;
import nl.inholland.bank.models.dtos.TransactionDTO.WithdrawRequest;
import nl.inholland.bank.repositories.TransactionRepository;
import org.springframework.stereotype.Service;

import javax.naming.InsufficientResourcesException;
import javax.security.auth.login.AccountNotFoundException;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }
    public Transaction createTransaction(User user, Account AccountSender, Account AccountReceiver, CurrencyType currencyType, double amount) {
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setAccountSender(AccountSender);
        transaction.setAccountReceiver(AccountReceiver);
        transaction.setCurrencyType(currencyType);
        transaction.setAmount(amount);

        return transaction;
    }

    public Transaction withdrawMoney(User user, Account account, double amount) throws AccountNotFoundException, InsufficientResourcesException {
        // Check if the account exists
        if (checkAccountExist(account)){
            // Check if the account has enough balance
            if (checkAccountBalance(account, amount)){
                // Update the account balance
                Transaction transaction = createTransaction(user, account, null, account.getCurrencyType(), amount);
                transactionRepository.save(transaction);

                // Update the account balance
                updateAccountBalance(account, amount, false);
                //TODO: Update the account repository with the new balance

                return transaction;
            }
            else {
                throw new InsufficientResourcesException("Account does not have enough balance");
            }
        }
        else {
            throw new AccountNotFoundException("Account does not exist");
        }
    }

    public void depositMoney(Account account, double amount) {
        if (checkAccountExist(account)) {
            return;
        }

        if (checkAccountBalance(account, amount)) {
            updateAccountBalance(account, amount, true);
            Transaction transaction = createTransaction(account.getUser(), null, account, account.getCurrencyType(), amount);
        }
    }

    public boolean checkAccountExist(Account account) {
        if (account != null) {
            System.out.println("Account does exist");
            return true;
        }
        return false;
    }

    public void transferMoney(User user, Account accountSender, Account accountReceiver, double amount, CurrencyType currencyType){
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
    }

    public Transaction mapWithdrawRequestToTransaction(WithdrawRequest withdrawRequest) {
        Transaction transaction = new Transaction();
        transaction.setAmount(withdrawRequest.amount());
        transaction.setCurrencyType(CurrencyType.EURO);
        return transaction;
    }
}
