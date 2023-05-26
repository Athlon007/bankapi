package nl.inholland.bank.controllers;

import nl.inholland.bank.models.Account;
import nl.inholland.bank.models.Role;
import nl.inholland.bank.models.User;
import nl.inholland.bank.models.dtos.AccountDTO.AccountRequest;
import nl.inholland.bank.models.dtos.AccountDTO.AccountResponse;
import nl.inholland.bank.services.AccountService;
import nl.inholland.bank.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/accounts")
public class AccountController {
    private AccountService accountService;
    private UserService userService;

    public AccountController(AccountService accountService, UserService userService) {
        this.accountService = accountService;
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity getAllAccountsByUserId(
            @PathVariable int id
    ) {
        try {
            User user = userService.getUserById(id);

            // Employee/Admin and the owner of the account can see the account
            if (userService.getBearerUserRole() == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            List<Account> accounts = accountService.getAccountsByUserId(user);

            if (accounts.isEmpty()) {
                // Return a custom response when there are no accounts
                return ResponseEntity.status(404).body("No accounts found");
            } else {
                List<AccountResponse> accountResponses = new ArrayList<>();
                // Return the list of accounts
                for (Account account : accounts) {
                    AccountResponse accountResponse = new AccountResponse(
                            account.getId(),
                            account.getIBAN(),
                            account.getType().toString(),
                            account.getCurrencyType().toString(),
                            account.getBalance()
                    );
                    accountResponses.add(accountResponse);
                }
                return ResponseEntity.status(200).body(accountResponses);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PostMapping
    public ResponseEntity addAccount(@RequestBody AccountRequest accountRequest) {
        if (userService.getBearerUserRole() != Role.EMPLOYEE) {
            return ResponseEntity.status(401).body("Unauthorized");
        } else {
            try{
                Account account = accountService.addAccount(accountRequest);

                AccountResponse accountResponse = new AccountResponse(
                        account.getId(),
                        account.getIBAN(),
                        account.getType().toString(),
                        account.getCurrencyType().toString(),
                        account.getBalance()
                );

                return ResponseEntity.status(201).body(accountResponse);
            }
            catch (Exception e){
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }
    }
}
