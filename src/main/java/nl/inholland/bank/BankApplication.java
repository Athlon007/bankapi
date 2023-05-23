package nl.inholland.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BankApplication {

	public static void main(String[] args) {
		System.out.println("Classpath: " + System.getProperty("java.class.path"));
		SpringApplication.run(BankApplication.class, args);
	}

}
