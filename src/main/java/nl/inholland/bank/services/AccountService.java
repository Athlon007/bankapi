package nl.inholland.bank.services;

import nl.inholland.bank.models.Account;
import nl.inholland.bank.models.AccountType;
import nl.inholland.bank.models.CurrencyType;
import nl.inholland.bank.models.User;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    public Account createAccount(User user, String IBAN, AccountType accountType, CurrencyType currencyType){
        Account account = new Account();
        account.setUser(user);
        account.setIBAN(IBAN);
        account.setType(accountType);
        account.setCurrencyType(currencyType);
        account.setBalance(0);
        account.setActive(true);

        return account;
    }

    public void DeactivateAccount(Account account){
        account.setActive(false);
    }

    public boolean isActive(Account account){
        return account.isActive();
    }

}
