package nl.inholland.bank.controllers;

import nl.inholland.bank.models.Account;
import nl.inholland.bank.models.Role;
import nl.inholland.bank.models.User;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/accounts")
@CrossOrigin(origins = "*")
public class AccountController {
    private AccountService accountService;
    private UserService userService;

    public AccountController(AccountService accountService, UserService userService) {
        this.accountService = accountService;
        this.userService = userService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity getAllAccountsByUserId(
            @PathVariable int userId
    ) {
        try {
            User user = userService.getUserById(userId);

            // Employee/Admin and the owner of the account can see the account
            if (userService.getBearerUserRole() != Role.EMPLOYEE && userService.getBearerUserRole() != Role.ADMIN && !Objects.equals(userService.getBearerUsername(), user.getUsername())) {
                return ResponseEntity.badRequest().body(new ExceptionResponse("Unauthorized request"));
            }

            List<Account> accounts = accountService.getAccountsByUserId(user);

            if (accounts.isEmpty()) {
                return ResponseEntity.badRequest().body(new ExceptionResponse("No account found"));

            } else {
                List<AccountResponse> accountResponses = new ArrayList<>();
                // Return the list of accounts
                for (Account account : accounts) {
                    AccountResponse accountResponse = new AccountResponse(
                            account.getId(),
                            account.getIBAN().toString(),
                            account.getCurrencyType().toString(),
                            account.getType().toString(),
                            account.isActive(),
                            account.getBalance()
                    );
                    accountResponses.add(accountResponse);
                }
                return ResponseEntity.status(200).body(accountResponses);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ExceptionResponse(e.getMessage()));
        }
    }


    @PostMapping
    public ResponseEntity addAccount(@RequestBody AccountRequest accountRequest) throws AuthenticationException {
        if (userService.getBearerUserRole() != Role.EMPLOYEE && userService.getBearerUserRole() != Role.ADMIN) {
            throw new AuthenticationException("Unauthorized");
        } else {
            try {
                Account account = accountService.addAccount(accountRequest);

                AccountResponse accountResponse = new AccountResponse(
                        account.getId(),
                        account.getIBAN().toString(),
                        account.getCurrencyType().toString(),
                        account.getType().toString(),
                        account.isActive(),
                        account.getBalance()
                );

                return ResponseEntity.status(201).body(accountResponse);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(new ExceptionResponse(e.getMessage()));
            }
        }
    }

    @PutMapping("/{userId}/{id}")
    public ResponseEntity activateAccount(@PathVariable int userId, @PathVariable int id,
                                          @RequestBody AccountActiveRequest accountActiveRequest) throws AuthenticationException {
        if (userService.getBearerUserRole() != Role.EMPLOYEE && userService.getBearerUserRole() != Role.ADMIN) {
            throw new AuthenticationException("Unauthorized");
        } else {
            try {
                User user = userService.getUserById(userId);
                Account account = accountService.getAccountById(id);

                if (account.getUser().getId() != user.getId()) {
                    return ResponseEntity.status(401).body("Unauthorized");
                }

                accountService.activateOrDeactivateTheAccount(account, accountActiveRequest);

                AccountResponse accountResponse = new AccountResponse(
                        account.getId(),
                        account.getIBAN().toString(),
                        account.getCurrencyType().toString(),
                        account.getType().toString(),
                        account.isActive(),
                        account.getBalance()
                );

                return ResponseEntity.status(200).body(accountResponse);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(new ExceptionResponse(e.getMessage()));
            }
        }
    }
}
