package nl.inholland.bank.configurators;

import jakarta.transaction.Transactional;
import nl.inholland.bank.models.User;
import nl.inholland.bank.models.dtos.UserRequest;
import nl.inholland.bank.services.UserService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

public class ApplicaitonDataInitializer implements ApplicationRunner {
    private final UserService userService;

    public ApplicaitonDataInitializer(UserService userService) {
        this.userService = userService;
    }

    @Transactional
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // TODO: Add data to the database

        UserRequest adminRequest = new UserRequest(
                "admin@example.com",
                "admin",
                "password",
                "Namey",
                "McNameface",
                "123456789",
                "0612345678",
                "2000-01-01"
        );

        userService.addUser(adminRequest);
    }
}
