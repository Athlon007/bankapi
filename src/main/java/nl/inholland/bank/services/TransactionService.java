package nl.inholland.bank.services;

import nl.inholland.bank.models.*;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {
    public Transaction createTransaction(User user, Account AccountSender, Account AccountReceiver, CurrencyType currencyType, double amount) {
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setAccountSender(AccountSender);
        transaction.setAccountReceiver(AccountReceiver);
        transaction.setCurrencyType(currencyType);
        transaction.setAmount(amount);

        return transaction;
    }

    public void withdrawMoney(Account account, double amount) {
        if (checkAccountExist(account)) {
            return;
        }
        if (checkAccountBalance(account, amount)) {
            updateAccountBalance(account, amount, false);
            // This transaction will be added to database
            Transaction transaction = createTransaction(account.getUser(), account, null, account.getCurrencyType(), amount);
        } else {
            System.out.println("Insufficient funds");
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
}
