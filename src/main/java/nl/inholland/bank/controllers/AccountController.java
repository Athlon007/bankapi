package nl.inholland.bank.controllers;

import nl.inholland.bank.models.Account;
import nl.inholland.bank.models.Role;
import nl.inholland.bank.models.User;
import nl.inholland.bank.models.dtos.AccountDTO.*;
import nl.inholland.bank.models.dtos.ExceptionResponse;
import nl.inholland.bank.services.AccountService;
import nl.inholland.bank.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
import javax.security.auth.login.AccountNotFoundException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/accounts")
@CrossOrigin(origins = "*")
public class AccountController {
    private final AccountService accountService;
    private final UserService userService;

    public AccountController(AccountService accountService, UserService userService) {
        this.accountService = accountService;
        this.userService = userService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity getAllAccountsByUserId(@PathVariable int userId) throws AuthenticationException {

        User user = userService.getUserById(userId);
        if (userService.getBearerUserRole() != Role.EMPLOYEE &&
                userService.getBearerUserRole() != Role.ADMIN
                && !Objects.equals(userService.getBearerUsername(), user.getUsername())) {
            throw new AuthenticationException("Unauthorized request");
        }

        List<Account> accounts = accountService.getAccountsByUserId(user);
        if (accounts.isEmpty()) {
            return ResponseEntity.badRequest().body(new ExceptionResponse("No account found"));
        }

        List<AccountResponse> accountResponses = accounts.stream()
                .map(this::buildAccountResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(accountResponses);
    }

    @PostMapping
    public ResponseEntity addAccount(@RequestBody AccountRequest accountRequest) throws AuthenticationException {
        if (userService.getBearerUserRole() != Role.EMPLOYEE && userService.getBearerUserRole() != Role.ADMIN) {
            throw new AuthenticationException("Unauthorized request");
        } else {
            Account account = accountService.addAccount(accountRequest);
            AccountResponse accountResponse = buildAccountResponse(account);

            return ResponseEntity.status(201).body(accountResponse);
        }

    }

    @PutMapping("/{userId}/{id}")
    public ResponseEntity activateAccount(@PathVariable int userId, @PathVariable int id,
                                          @RequestBody AccountActiveRequest accountActiveRequest) throws AccountNotFoundException, AuthenticationException {

        User user = userService.getUserById(userId);
        Account account = accountService.getAccountById(id);
        authenticateAndAuthorize(user, account);
        accountService.activateOrDeactivateTheAccount(account, accountActiveRequest);
        AccountResponse accountResponse = buildAccountResponse(account);

        return ResponseEntity.ok(accountResponse);
    }

    @PutMapping("/{userId}/{id}/limit")
    public ResponseEntity updateAbsoluteLimit(@PathVariable int userId, @PathVariable int id,
                                              @RequestBody AccountAbsoluteLimitRequest accountAbsoluteLimitRequest) throws AccountNotFoundException, AuthenticationException {
        User user = userService.getUserById(userId);
        Account account = accountService.getAccountById(id);
        authenticateAndAuthorize(user, account);

        accountService.updateAbsoluteLimit(account, accountAbsoluteLimitRequest);
        AccountResponse accountResponse = buildAccountResponse(account);

        return ResponseEntity.status(200).body(accountResponse);
    }

    private void authenticateAndAuthorize(User user, Account account) throws AuthenticationException {
        if (userService.getBearerUserRole() != Role.EMPLOYEE && userService.getBearerUserRole() != Role.ADMIN) {
            throw new AuthenticationException("Unauthorized request");
        }

        if (account.getUser().getId() != user.getId()) {
            throw new AuthenticationException("Unauthorized request");
        }
    }

    @GetMapping
    public ResponseEntity getAccounts(
            @RequestParam Optional<Integer> page,
            @RequestParam Optional<Integer> limit,
            @RequestParam Optional<String> iban,
            @RequestParam Optional<String> firstName,
            @RequestParam Optional<String> lastName,
            @RequestParam Optional<String> accountType
    ) {
        // Retrieve accounts
        List<Account> accounts = accountService.getAccounts(page, limit, iban, firstName, lastName, accountType);

        if (userService.getBearerUserRole() == Role.CUSTOMER) {
            // Convert to client responses
            List<AccountClientResponse> accountClientResponses = accounts.stream()
                    .map(this::buildAccountClientResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.status(200).body(accountClientResponses);
        } else {
            // Convert to account responses
            List<AccountResponse> accountResponses = accounts.stream()
                    .map(this::buildAccountResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.status(200).body(accountResponses);
        }
    }

    public AccountResponse buildAccountResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getIBAN(),
                account.getCurrencyType().toString(),
                account.getType().toString(),
                account.isActive(),
                account.getBalance(),
                account.getAbsoluteLimit(),
                account.getUser().getFirstName(),
                account.getUser().getLastName()
        );
    }

    public AccountClientResponse buildAccountClientResponse(Account account) {
        return new AccountClientResponse(
                account.getId(),
                account.getIBAN(),
                account.getCurrencyType().toString(),
                account.getType().toString(),
                account.isActive(),
                account.getUser().getFirstName(),
                account.getUser().getLastName()
        );
    }
}
