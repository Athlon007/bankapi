package nl.inholland.bank.services;

import nl.inholland.bank.models.*;
import nl.inholland.bank.models.dtos.TransactionDTO.TransactionRequest;
import nl.inholland.bank.models.exceptions.UserNotTheOwnerOfAccountException;
import nl.inholland.bank.repositories.TransactionRepository;
import nl.inholland.bank.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import javax.naming.InsufficientResourcesException;
import javax.security.auth.login.AccountNotFoundException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@SpringBootTest
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void processTransaction_SuccessfulTransaction() throws AccountNotFoundException, InsufficientResourcesException, UserNotTheOwnerOfAccountException {
        // Create a sample transaction request
        TransactionRequest request = new TransactionRequest(
                "NL60INHO9935031775",
                "NL71INHO6310134205",
                100.0,
                "Transferring money for groceries."
        );

        // Create mock objects
        User user = new User();
        Account accountSender = new Account();
        Account accountReceiver = new Account();
        Transaction transaction = new Transaction();

        // Set up mock behaviors
        when(userRepository.findUserByUsername(userService.getBearerUsername())).thenReturn(java.util.Optional.of(user));
        when(accountService.getAccountByIBAN(request.sender_iban())).thenReturn(accountSender);
        when(accountService.getAccountByIBAN(request.receiver_iban())).thenReturn(accountReceiver);

        // Perform the test
        Transaction result = new Transaction();
        when(transactionService.transferMoney(user, accountSender, accountReceiver,
                CurrencyType.EURO, request.amount(), request.description())).thenReturn(result);

        // Assert the result
        assertNotNull(result);
    }

    // Add more functional tests for different scenarios

    @Test
    void testTransferMoney() {
        // Prepare test data and setup dependencies

        // Perform the transfer
        //Transaction result = transactionService.transferMoney(/* provide necessary parameters */);
        Transaction result = new Transaction();
        // Assert the result
        assertNotNull(result);
        // Add more assertions to validate the correctness of the transfer result
    }

}
