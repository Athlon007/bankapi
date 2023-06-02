package nl.inholland.bank.services;

import nl.inholland.bank.models.*;
import nl.inholland.bank.models.dtos.AccountDTO.AccountRequest;
import nl.inholland.bank.repositories.AccountRepository;
import org.hibernate.ObjectNotFoundException;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import javax.security.auth.login.AccountNotFoundException;
import java.util.List;
import java.util.Objects;

@Service
public class AccountService {
    AccountRepository accountRepository;

    UserService userService;

    public AccountService(AccountRepository accountRepository, UserService userService) {
        this.accountRepository = accountRepository;
        this.userService = userService;
    }

    public Account createAccount(User user, String IBAN, AccountType accountType, CurrencyType currencyType) {
        Account account = new Account();
        account.setUser(user);
        account.setIBAN(IBAN);
        account.setType(accountType);
        account.setCurrencyType(currencyType);
        account.setBalance(0);
        account.setActive(true);

        return account;
    }

    public boolean isActive(Account account) {
        return account.isActive();
    }


    public Account getAccountByIBAN(String iban) throws AccountNotFoundException {
        if (IBANGenerator.isValidIBAN(iban)) {
            return accountRepository.findByIBAN(iban)
                    .orElseThrow(() -> new AccountNotFoundException("Account not found."));
        } else {
            return null;
        }
    }

    public Account addAccount(AccountRequest accountRequest) {
        User user = null;
        user = userService.getUserById(accountRequest.userId());

        if (!IBANGenerator.isValidIBAN(accountRequest.IBAN())) {
            throw new IllegalArgumentException("IBAN is not valid");
        }

        AccountType accountType = mapAccountTypeToString(accountRequest.accountType());
        if (accountType == AccountType.CURRENT) {
            if (doesUserHaveAccountType(user, AccountType.CURRENT)) {
                throw new IllegalArgumentException("User already has a current account");
            }
        } else if (accountType == AccountType.SAVING) {
            if (!doesUserHaveAccountType(user, AccountType.CURRENT)) {
                throw new IllegalArgumentException("User does not have a current account");
            }
            if (doesUserHaveAccountType(user, AccountType.SAVING)) {
                throw new IllegalArgumentException("User already has a saving account");
            }
        }

        Account account = createAccount(
                user,
                accountRequest.IBAN(),
                accountType,
                mapCurrencyTypeToString(accountRequest.currencyType())
        );
        Account responseAccount = accountRepository.save(account);
        userService.assignAccountToUser(user, responseAccount);

        return responseAccount;
    }

    // Get all accounts that belong to a user (max. 1 current account and max. 1 saving account)
    public List<Account> getAccountsByUserId(User user) {
        return accountRepository.findAllByUser(user);
    }

    // Check if the user has a certain account type, returns true if the user has the account type
    public boolean doesUserHaveAccountType(User user, AccountType accountType) {
        List<Account> accounts = getAccountsByUserId(user);
        for (Account account : accounts) {
            if (account.getType().equals(accountType)) {
                return true;
            }
        }
        return false;
    }

    public CurrencyType mapCurrencyTypeToString(String currencyType) {
        if (currencyType.equals("EURO")) {
            return CurrencyType.EURO;
        }
        throw new IllegalArgumentException("Invalid currencyType: " + currencyType);
    }

    public AccountType mapAccountTypeToString(String accountType) {
        return switch (accountType) {
            case "CURRENT" -> AccountType.CURRENT;
            case "SAVING" -> AccountType.SAVING;
            default -> throw new IllegalArgumentException("Invalid accountType: " + accountType);
        };
    }


    public Account getAccountByIban(String iban) {
        return accountRepository.findByIBAN(iban).orElseThrow(() -> new IllegalArgumentException("Account not found"));
    }

    public void updateAccount(Account account) {
        accountRepository.save(account);
    }

    public Account getAccountById(int id) {
        return accountRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Account not found"));
    }

    public void activateOrDeactivateTheAccount(Account account, boolean isActive) {
        account.setActive(isActive);
        accountRepository.save(account);
    }




}
