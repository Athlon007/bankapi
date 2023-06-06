package nl.inholland.bank.services;

import nl.inholland.bank.models.*;
import nl.inholland.bank.models.dtos.AccountDTO.AccountAbsoluteLimitRequest;
import nl.inholland.bank.models.dtos.AccountDTO.AccountActiveRequest;
import nl.inholland.bank.models.dtos.AccountDTO.AccountRequest;
import nl.inholland.bank.repositories.AccountRepository;
import org.springframework.stereotype.Service;

import javax.security.auth.login.AccountNotFoundException;
import java.util.List;

@Service
public class AccountService {
    AccountRepository accountRepository;

    UserService userService;

    public AccountService(AccountRepository accountRepository, UserService userService) {
        this.accountRepository = accountRepository;
        this.userService = userService;
    }

    public Account createAccount(User user, AccountType accountType, CurrencyType currencyType) {
        Account account = new Account();
        account.setUser(user);
        account.setIBAN(IBANGenerator.generateIBAN().toString());
        account.setType(accountType);
        account.setCurrencyType(currencyType);
        account.setBalance(0);
        account.setActive(true);
        account.setAbsoluteLimit(0);

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
            throw new IllegalArgumentException("Invalid IBAN provided.");
        }
    }


    public Account addAccount(AccountRequest accountRequest) {
        User user = null;
        user = userService.getUserById(accountRequest.userId());

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

    public boolean doesUserHaveAccountType(User user, AccountType accountType) {
        return getAccountsByUserId(user).stream().anyMatch(account -> account.getType().equals(accountType));
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


    public void updateAccount(Account account) {
        accountRepository.save(account);
    }

    public Account getAccountById(int id) throws AccountNotFoundException {
        return accountRepository.findById(id).orElseThrow(() -> new AccountNotFoundException("Account not found"));
    }

    public void activateOrDeactivateTheAccount(Account account, AccountActiveRequest accountActiveRequest) {
        account.setActive(accountActiveRequest.isActive());
        accountRepository.save(account);
    }

    public void updateAbsoluteLimit(Account account, AccountAbsoluteLimitRequest accountAbsoluteLimitRequest) {
        account.setAbsoluteLimit(accountAbsoluteLimitRequest.absoluteLimit());
        accountRepository.save(account);
    }
}
