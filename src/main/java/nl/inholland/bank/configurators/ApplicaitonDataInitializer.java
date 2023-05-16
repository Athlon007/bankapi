package nl.inholland.bank.configurators;

import jakarta.transaction.Transactional;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

public class ApplicaitonDataInitializer implements ApplicationRunner {
    @Transactional
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // TODO: Add data to the database
    }
}
