package nl.inholland.bank.controllers;

import nl.inholland.bank.models.Account;
import nl.inholland.bank.models.User;
import nl.inholland.bank.models.dtos.AccountDTO.AccountRequest;
import nl.inholland.bank.services.AccountService;
import nl.inholland.bank.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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
            @RequestParam(required = false) Optional<Integer> page,
            @RequestParam(required = false) Optional<Integer> limit,
            @RequestParam Optional<String> IBAN,
            @PathVariable int id
    ) {
        try {
            id = 3;
            User user = userService.getUserById(id);
            List<Account> accounts = accountService.getAllAccountsFromUser(page, limit, IBAN, user);

            if (accounts.isEmpty()) {
                // Return a custom response when there are no accounts
                return ResponseEntity.ok("No accounts found for the user.");
            } else {
                // Return the list of accounts
                return ResponseEntity.ok(accounts);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PostMapping
    public ResponseEntity addAccount(@RequestBody AccountRequest accountRequest) {
        Account account = accountService.addAccount(accountRequest, userService.getUserById(3));
        return ResponseEntity.ok(account);
    }
}
