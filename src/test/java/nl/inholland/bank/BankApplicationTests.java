package nl.inholland.bank;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BankApplicationTests {

	@Test
	void contextLoads() {
		Mockito.mock(BankApplication.class);

		BankApplication.main(new String[] {});
	}

}
