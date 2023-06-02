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
        // TODO: Add data to the database

        UserForAdminRequest adminRequest = new UserForAdminRequest(
                "admin@example.com",
                "admin",
                "Password1!",
                "Namey",
                "McNameface",
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
        //System.out.println(userService.getAllUsers(Optional.empty(), Optional.empty(), Optional.empty()));

        // ----------------
        // --- ACCOUNTS ---
        // ----------------

        AccountRequest accountRequest = new AccountRequest(
                "NL71INHO6310134205",
                "EURO",
                "CURRENT",
                1);

        accountService.addAccount(accountRequest);

        // Test for transfering money
        // Account for employee
        AccountRequest accountRequest2 = new AccountRequest(
                "NL60INHO9935031775",
                "EURO",
                "CURRENT",
                3);
        accountService.addAccount(accountRequest2);


        //Transaction Withdraw and Deposit
        WithdrawDepositRequest withdrawDepositRequest = new WithdrawDepositRequest(
                "NL60INHO9935031775",
                300,
                CurrencyType.EURO,
                3);

        try {
            transactionService.depositMoney(withdrawDepositRequest);
        } catch (UserNotTheOwnerOfAccountException e) {
            throw new RuntimeException(e);
        }

       Account accountSender = accountService.getAccountByIban(withdrawDepositRequest.IBAN());

        try {
            transactionService.withdrawMoney(withdrawDepositRequest);
        } catch (UserNotTheOwnerOfAccountException e) {
            throw new RuntimeException(e);
        }
    }
}
