package nl.inholland.bank.services;

import nl.inholland.bank.models.*;
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

    public Account getAccountByIBAN(String iban)
    {
        // Check if iban is valid
        if (IBANGenerator.isValidIBAN(iban)) {
            // Try to find an account
            return accountRepository.findAccountByIBAN(iban);
        } else { // Invalid iban
            return null;
        }
    }

    public Account addAccount(AccountRequest accountRequest){
        Account account = mapAccountRequestToAccount(accountRequest);

        return accountRepository.save(account);
    }

    // Get all accounts from a user
    public List<Account> getAccountsByUserId(User user){

            return accountRepository.findAllByUser(user);
    }

    public Account mapAccountRequestToAccount(AccountRequest accountRequest){
        Account account = new Account();
        User user = userService.getUserById(Integer.parseInt(accountRequest.userId()));
        account.setUser(user);
        account.setIBAN(accountRequest.IBAN());
        account.setType(mapAccountTypeToString(accountRequest.accountType()));
        account.setCurrencyType(mapCurrencyTypeToString(accountRequest.currencyType()));
        account.setBalance(accountRequest.balance());
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
