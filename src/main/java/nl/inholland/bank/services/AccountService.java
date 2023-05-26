package nl.inholland.bank.services;

import nl.inholland.bank.models.Account;
import nl.inholland.bank.models.AccountType;
import nl.inholland.bank.models.CurrencyType;
import nl.inholland.bank.models.User;
import nl.inholland.bank.models.dtos.AccountDTO.AccountRequest;
import nl.inholland.bank.repositories.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {
    AccountRepository accountRepository;

    UserService userService;

    public AccountService(AccountRepository accountRepository, UserService userService) {
        this.accountRepository = accountRepository;
        this.userService = userService;
    }

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

    public Account getAccountById(int id){
        return null;
    }

    public Account addAccount(AccountRequest accountRequest){
        if (mapAccountTypeToString(accountRequest.accountType()) == AccountType.CURRENT){
            if (doesUserHaveCurrentAccount(userService.getUserById(Integer.parseInt(accountRequest.userId())))){
                throw new IllegalArgumentException("User already has a current account");
            }
        }
        if (mapAccountTypeToString(accountRequest.accountType()) == AccountType.SAVING){
            if (!doesUserHaveCurrentAccount(userService.getUserById(Integer.parseInt(accountRequest.userId())))){
                throw new IllegalArgumentException("You cannot create a saving account without a current account");
            }
            if (doesUserHaveSavingAccount(userService.getUserById(Integer.parseInt(accountRequest.userId())))){
                throw new IllegalArgumentException("User already has a saving account");
            }
        }

        Account account = mapAccountRequestToAccount(accountRequest);

        return accountRepository.save(account);
    }

    // Get all accounts from a user
    public List<Account> getAccountsByUserId(User user){

            return accountRepository.findAllByUser(user);
    }

    public boolean doesUserHaveCurrentAccount(User user){
        List<Account> accounts = getAccountsByUserId(user);
        for (Account account : accounts) {
            if (account.getType() == AccountType.CURRENT){
                return true;
            }
        }
        return false;
    }

    public boolean doesUserHaveSavingAccount(User user){
        List<Account> accounts = getAccountsByUserId(user);
        for (Account account : accounts) {
            if (account.getType() == AccountType.SAVING){
                return true;
            }
        }
        return false;
    }

    public Account mapAccountRequestToAccount(AccountRequest accountRequest){
        Account account = new Account();
        User user = userService.getUserById(Integer.parseInt(accountRequest.userId()));
        account.setUser(user);
        account.setIBAN(accountRequest.IBAN());
        account.setType(mapAccountTypeToString(accountRequest.accountType()));
        account.setCurrencyType(mapCurrencyTypeToString(accountRequest.currencyType()));
        account.setBalance(0);
        account.setActive(true);

        return account;
    }

    public CurrencyType mapCurrencyTypeToString(String currencyType){
        switch (currencyType){
            case "EUR":
                return CurrencyType.EURO;
            default:
                return CurrencyType.EURO;
        }
    }

    public AccountType mapAccountTypeToString(String accountType){
        switch (accountType){
            case "CURRENT":
                return AccountType.CURRENT;
            case "SAVING":
                return AccountType.SAVING;
            default:
                return AccountType.CURRENT;
        }
    }

}
