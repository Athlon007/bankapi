package nl.inholland.bank.configurators;

import jakarta.transaction.Transactional;
import nl.inholland.bank.models.User;
import nl.inholland.bank.models.dtos.UserForAdminRequest;
import nl.inholland.bank.models.dtos.UserRequest;
import nl.inholland.bank.services.UserService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ApplicationDataInitializer implements ApplicationRunner {
    private final UserService userService;

    public ApplicationDataInitializer(UserService userService) {
        this.userService = userService;
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
                "123456789",
                "0612345678",
                "2000-01-01",
                "ADMIN"
        );

        User admin = userService.addUserForAdmin(adminRequest);

        UserForAdminRequest employeeRequest = new UserForAdminRequest(
                "employee@example.com",
                "employee",
                "Password2!",
                "Goofy",
                "Ahh",
                "123456789",
                "0612345678",
                "2000-01-01",
                "EMPLOYEE"
        );

        userService.addUserForAdmin(employeeRequest);

        UserRequest userRequest = new UserRequest(
                "client@example.com",
                "client",
                "Password3!",
                "Yo",
                "Mama",
                "123456789",
                "0612345678",
                "2000-01-01"
        );

        userService.addUser(userRequest);

        // Set empty optional to null




        System.out.println(userService.getAllUsers(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()));
    }
}
