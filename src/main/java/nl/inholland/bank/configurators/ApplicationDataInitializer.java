package nl.inholland.bank.configurators;

import jakarta.transaction.Transactional;
import nl.inholland.bank.models.Account;
import nl.inholland.bank.models.CurrencyType;
import nl.inholland.bank.models.Transaction;
import nl.inholland.bank.models.User;
import nl.inholland.bank.models.dtos.AccountDTO.AccountRequest;
import nl.inholland.bank.models.dtos.TransactionDTO.WithdrawDepositRequest;
import nl.inholland.bank.models.dtos.UserDTO.UserForAdminRequest;
import nl.inholland.bank.models.dtos.UserDTO.UserRequest;
import nl.inholland.bank.models.exceptions.UserNotTheOwnerOfAccountException;
import nl.inholland.bank.services.AccountService;
import nl.inholland.bank.services.TransactionService;
import nl.inholland.bank.services.UserService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ApplicationDataInitializer implements ApplicationRunner {
    private final UserService userService;
    private final AccountService accountService;

    private final TransactionService transactionService;

    public ApplicationDataInitializer(UserService userService, AccountService accountService, TransactionService transactionService) {
        this.userService = userService;
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    @Transactional
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // ----------------
        // ---   USERS  ---
        // ----------------

        UserForAdminRequest adminRequest = new UserForAdminRequest(
                "office@inhollandbank.nl",
                "admin",
                "Password1!",
                "InHolland",
                "Bank",
                "232262536",
                "0612345678",
                "2000-01-01",
                "ADMIN");

        User admin = userService.addAdmin(adminRequest);

        UserForAdminRequest employeeRequest = new UserForAdminRequest(
                "employee@example.com",
                "employee",
                "Password2!",
                "Goofy",
                "Ahh",
                "123456782",
                "0612345678",
                "2000-01-01",
                "EMPLOYEE");

        userService.addAdmin(employeeRequest);

        UserRequest userRequest = new UserRequest(
                "client@example.com",
                "client",
                "Password3!",
                "Yo",
                "Mama",
                "111222333",
                "0612345678",
                "2000-01-01");
        userService.addUser(userRequest);

        // Client Bobby
        UserRequest userBobbyRequest = new UserRequest(
                "bobby@example.com",
                "bobby",
                "Password4!",
                "Bobby",
                "Bob",
                "456123789",
                "0612345678",
                "2000-01-01");
        userService.addUser(userBobbyRequest);

        // Client Berta
        UserRequest userBertaRequest = new UserRequest(
                "berta@example.com",
                "berta",
                "Password4!",
                "Berta",
                "Bob",
                "789123654",
                "0612345678",
                "2000-01-01");
        userService.addUser(userBertaRequest);

        // ----------------
        // --- ACCOUNTS ---
        // ----------------

        // First admin account belongs to the bank.
        // Therefore, we assign it to the bank.
        accountService.addAccountForBank(admin);

        // Test for transferring money
        // Account for employee
        AccountRequest accountRequest2 = new AccountRequest(
                "EURO",
                "CURRENT",
                3);
        accountService.addAccount(accountRequest2);

        // Account for Bobby
        AccountRequest accountBobbyRequest = new AccountRequest(
                "EURO",
                "CURRENT",
                4);
        Account bobbyAccount = accountService.addAccount(accountBobbyRequest);

        // Account for Berta
        AccountRequest accountBertaRequest = new AccountRequest(
                "EURO",
                "CURRENT",
                5);
        Account bertaAccount = accountService.addAccount(accountBertaRequest);

        // Add money for the scenario accounts
        transactionService.updateAccountBalance(bobbyAccount, 100.00, true);
        transactionService.updateAccountBalance(bertaAccount, 300.00, true);

        System.out.println("=== Application Initialized ===");
    }
}
