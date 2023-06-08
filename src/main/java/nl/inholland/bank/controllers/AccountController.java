package nl.inholland.bank.controllers;

import nl.inholland.bank.models.Account;
import nl.inholland.bank.models.Role;
import nl.inholland.bank.models.User;
import nl.inholland.bank.models.dtos.AccountDTO.AccountAbsoluteLimitRequest;
import nl.inholland.bank.models.dtos.AccountDTO.AccountActiveRequest;
import nl.inholland.bank.models.dtos.AccountDTO.AccountRequest;
import nl.inholland.bank.models.dtos.AccountDTO.AccountResponse;
import nl.inholland.bank.models.dtos.ExceptionResponse;
import nl.inholland.bank.services.AccountService;
import nl.inholland.bank.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
import java.util.List;
import java.util.Objects;
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
    public ResponseEntity getAllAccountsByUserId(@PathVariable int userId) {
        try {
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
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ExceptionResponse(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity addAccount(@RequestBody AccountRequest accountRequest) throws AuthenticationException {
        try{
            if(userService.getBearerUserRole() != Role.EMPLOYEE && userService.getBearerUserRole() != Role.ADMIN){
                throw new AuthenticationException("Unauthorized request");
            }else {
                Account account = accountService.addAccount(accountRequest);
                AccountResponse accountResponse = buildAccountResponse(account);

                return ResponseEntity.status(201).body(accountResponse);
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ExceptionResponse(e.getMessage()));
        }
    }

    @PutMapping("/{userId}/{id}")
    public ResponseEntity activateAccount(@PathVariable int userId, @PathVariable int id,
                                          @RequestBody AccountActiveRequest accountActiveRequest) {
        try {
            User user = userService.getUserById(userId);
            Account account = accountService.getAccountById(id);
            authenticateAndAuthorize(user, account);

            accountService.activateOrDeactivateTheAccount(account, accountActiveRequest);
            AccountResponse accountResponse = buildAccountResponse(account);

            return ResponseEntity.ok(accountResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ExceptionResponse(e.getMessage()));
        }
    }

    @PutMapping("/{userId}/{id}/limit")
    public ResponseEntity updateAbsoluteLimit(@PathVariable int userId, @PathVariable int id,
                                              @RequestBody AccountAbsoluteLimitRequest accountAbsoluteLimitRequest) {
        try {
            User user = userService.getUserById(userId);
            Account account = accountService.getAccountById(id);
            authenticateAndAuthorize(user, account);

            accountService.updateAbsoluteLimit(account, accountAbsoluteLimitRequest);
            AccountResponse accountResponse = buildAccountResponse(account);

            return ResponseEntity.status(200).body(accountResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ExceptionResponse(e.getMessage()));
        }
    }

    private void authenticateAndAuthorize(User user, Account account) throws AuthenticationException {
        if (userService.getBearerUserRole() != Role.EMPLOYEE && userService.getBearerUserRole() != Role.ADMIN) {
            throw new AuthenticationException("Unauthorized request");
        }

        if (account.getUser().getId() != user.getId()) {
            throw new AuthenticationException("Unauthorized request");
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
                account.getAbsoluteLimit()
        );
    }
}
