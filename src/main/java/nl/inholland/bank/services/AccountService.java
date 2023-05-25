package nl.inholland.bank.services;

import nl.inholland.bank.models.Account;
import nl.inholland.bank.models.AccountType;
import nl.inholland.bank.models.CurrencyType;
import nl.inholland.bank.models.User;
import nl.inholland.bank.models.dtos.AccountDTO.AccountRequest;
import nl.inholland.bank.repositories.AccountRepository;
import org.hibernate.ObjectNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    public Account addAccount(AccountRequest accountRequest, User user){
        Account account = mapAccountRequestToAccount(accountRequest, userService.getUserById(3));

        return accountRepository.save(account);
    }

    // Get all accounts from a user
    public List<Account> getAllAccountsFromUser(Optional<Integer> page, Optional<Integer> limit, Optional<String> IBAN, User user){
        int pageNumber = page.orElse(0);
        int limitNumber = limit.orElse(10);

        Pageable pageable = PageRequest.of(pageNumber, limitNumber);

        if(IBAN.isPresent()){
            return IBAN.map(
                i -> accountRepository.findAllAccountsByUser(user, pageable).getContent()
            ).orElseThrow(() -> new ObjectNotFoundException(IBAN, "IBAN"));
        }
        else {
            return accountRepository.findAllAccountsByUser(user, pageable).getContent();
        }
    }

    public Account mapAccountRequestToAccount(AccountRequest accountRequest, User user){
        Account account = new Account();
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
            case "SAVINGS":
                return AccountType.SAVING;
            default:
                return AccountType.CURRENT;
        }
    }

}
