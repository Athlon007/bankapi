package nl.inholland.bank.configurators;

import jakarta.transaction.Transactional;
import nl.inholland.bank.models.User;
import nl.inholland.bank.models.dtos.AccountDTO.AccountRequest;
import nl.inholland.bank.models.dtos.UserDTO.UserForAdminRequest;
import nl.inholland.bank.models.dtos.UserDTO.UserRequest;
import nl.inholland.bank.services.AccountService;
import nl.inholland.bank.services.UserService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ApplicationDataInitializer implements ApplicationRunner {
    private final UserService userService;
    private final AccountService accountService;

    public ApplicationDataInitializer(UserService userService, AccountService accountService) {
        this.userService = userService;
        this.accountService = accountService;
    }

    @Transactional
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // TODO: Add data to the database

        UserForAdminRequest adminRequest = new UserForAdminRequest(
                "admin@example.com",
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
        //System.out.println(userService.getAllUsers(Optional.empty(), Optional.empty(), Optional.empty()));

        // ----------------
        // --- ACCOUNTS ---
        // ----------------

        AccountRequest accountRequest = new AccountRequest(
                "EURO",
                "CURRENT",
                1);

        accountService.addAccount(accountRequest);

        // Test for transfering money
        // Account for employee
        AccountRequest accountRequest2 = new AccountRequest(
                "EURO",
                "CURRENT",
                3);
        accountService.addAccount(accountRequest2);
    }
}
