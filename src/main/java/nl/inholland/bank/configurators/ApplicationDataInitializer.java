package nl.inholland.bank.configurators;

import jakarta.transaction.Transactional;
import nl.inholland.bank.models.User;
import nl.inholland.bank.models.dtos.UserForAdminRequest;
import nl.inholland.bank.models.dtos.UserRequest;
import nl.inholland.bank.services.UserService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

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
                "password",
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
                "password1",
                "Yo",
                "Lo",
                "123456789",
                "0612345678",
                "2000-01-01",
                "EMPLOYEE"
        );

        userService.addUserForAdmin(employeeRequest);

        UserRequest userRequest = new UserRequest(
                "client@example.com",
                "client",
                "password2",
                "Yo",
                "Mama",
                "123456789",
                "0612345678",
                "2000-01-01"
        );

        userService.addUser(userRequest);

        System.out.println(userService.getAllUsers());
    }
}
