package nl.inholland.bank.services;

import nl.inholland.bank.configuration.ApiTestConfiguration;
import nl.inholland.bank.models.*;
import nl.inholland.bank.models.dtos.UserDTO.UserLimitsRequest;
import nl.inholland.bank.repositories.TransactionRepository;
import nl.inholland.bank.repositories.UserLimitsRepository;
import nl.inholland.bank.repositories.UserRepository;
import nl.inholland.bank.utils.JwtTokenProvider;
import org.hibernate.ObjectNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.MethodNotAllowedException;

import javax.naming.AuthenticationException;
import java.time.LocalDate;
import java.util.List;

@ExtendWith(SpringExtension.class)
@Import(ApiTestConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class UserLimitsServiceTests {
    private UserLimitsService userLimitsService;

    @MockBean
    private UserLimitsRepository userLimitsRepository;
    @Autowired
    private JwtTokenProvider mockJwtTokenProvider;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private TransactionRepository transactionRepository;

    private User user;
    private User receiverUser;
    private User userWithNoAccount;
    private Limits limits;
    private Transaction sendTransaction;
    private Transaction receiveTransaction;
    private Transaction withdrawal;
    private Transaction deposit;
    private Account userAccount;
    private Account receiverAccount;
    private UserLimitsRequest userLimitsRequest;

    @BeforeEach
    void setUp() {
        userLimitsService = new UserLimitsService(userLimitsRepository, mockJwtTokenProvider, userRepository, transactionRepository);

        user = new User();
        user.setId(1);
        user.setUsername("user");
        user.setEmail("email@ex.com");
        user.setFirstName("first");
        user.setLastName("last");
        user.setPhoneNumber("0612345678");
        user.setDateOfBirth(LocalDate.of(2000, 9, 8));
        user.setPassword("Password1!");
        user.setRole(Role.USER);
        user.setBsn("123456782");

        receiverUser = new User();
        receiverUser.setId(2);
        receiverUser.setUsername("user");
        receiverUser.setEmail("email@ex.com");
        receiverUser.setFirstName("first");
        receiverUser.setLastName("last");
        receiverUser.setPhoneNumber("0612345678");
        receiverUser.setDateOfBirth(LocalDate.of(2000, 9, 8));
        receiverUser.setPassword("Password1!");
        receiverUser.setRole(Role.USER);
        receiverUser.setBsn("123456782");

        userWithNoAccount = new User();
        userWithNoAccount.setId(3);
        userWithNoAccount.setUsername("user3");
        userWithNoAccount.setEmail("email@ex.com");
        userWithNoAccount.setFirstName("first");
        userWithNoAccount.setLastName("last");
        userWithNoAccount.setPhoneNumber("0612345678");
        userWithNoAccount.setDateOfBirth(LocalDate.of(2000, 9, 8));
        userWithNoAccount.setPassword("Password1!");
        userWithNoAccount.setRole(Role.USER);
        userWithNoAccount.setBsn("123456782");

        userAccount = new Account();
        userAccount.setType(AccountType.CURRENT);
        userAccount.setBalance(1000);
        userAccount.setIBAN("NL01INHO0000000001");
        userAccount.setUser(user);
        userAccount.setId(1);
        userAccount.setCurrencyType(CurrencyType.EURO);
        userAccount.setActive(true);

        receiverAccount = new Account();
        receiverAccount.setType(AccountType.CURRENT);
        receiverAccount.setBalance(1000);
        receiverAccount.setIBAN("NL01INHO0000000002");
        receiverAccount.setUser(user);
        receiverAccount.setId(2);
        receiverAccount.setCurrencyType(CurrencyType.EURO);
        receiverAccount.setActive(true);

        limits = new Limits();
        limits.setTransactionLimit(1000);
        limits.setDailyTransactionLimit(1000);
        limits.setAbsoluteLimit(0);
        limits.setRemainingDailyTransactionLimit(1000);

        sendTransaction = new Transaction();
        sendTransaction.setId(1);
        sendTransaction.setAmount(100);
        sendTransaction.setTimestamp(LocalDate.now().atStartOfDay());
        sendTransaction.setAccountReceiver(receiverAccount);
        sendTransaction.setAccountSender(userAccount);

        receiveTransaction = new Transaction();
        receiveTransaction.setId(2);
        receiveTransaction.setAmount(100);
        receiveTransaction.setTimestamp(LocalDate.now().atStartOfDay());
        receiveTransaction.setAccountReceiver(receiverAccount);
        receiveTransaction.setAccountSender(userAccount);

        withdrawal = new Transaction();
        withdrawal.setId(3);
        withdrawal.setAmount(5);
        withdrawal.setTimestamp(LocalDate.now().atStartOfDay());
        withdrawal.setAccountReceiver(userAccount);
        withdrawal.setAccountSender(null);

        deposit = new Transaction();
        deposit.setId(4);
        deposit.setAmount(5);
        deposit.setTimestamp(LocalDate.now().atStartOfDay());
        deposit.setAccountReceiver(null);
        deposit.setAccountSender(userAccount);


        user.setCurrentAccount(userAccount);
        receiverUser.setCurrentAccount(receiverAccount);

        userLimitsRequest = new UserLimitsRequest(1000, 1000, 0);
    }

    @Test
    void getUserLimitsNoAuthReturnsLimits() {
        Mockito.when(userLimitsRepository.findFirstByUserId(Mockito.anyInt())).thenReturn(limits);
        Mockito.when(transactionRepository.findAllByTimestampIsAfterAndUserId(Mockito.any(), Mockito.anyInt())).thenReturn(java.util.List.of(sendTransaction, receiveTransaction, withdrawal, deposit));

        Assertions.assertDoesNotThrow(() -> userLimitsService.getUserLimitsNoAuth(1));
    }

    @Test
    void getUsersLimitsNoAuthForUserThatDoesNotExistThrowsException() {
        Mockito.when(userLimitsRepository.findFirstByUserId(Mockito.anyInt())).thenReturn(null);

        Assertions.assertThrows(ObjectNotFoundException.class, () -> userLimitsService.getUserLimitsNoAuth(1));
    }

    @Test
    void getUserLimitsReturnsLimits() {
        Mockito.when(userLimitsRepository.findFirstByUserId(Mockito.anyInt())).thenReturn(limits);
        Mockito.when(transactionRepository.findAllByTimestampIsAfterAndUserId(Mockito.any(), Mockito.anyInt())).thenReturn(java.util.List.of(sendTransaction, receiveTransaction, withdrawal, deposit));

        Mockito.when(userRepository.findById(Mockito.anyInt())).thenReturn(java.util.Optional.of(user));
        Mockito.when(mockJwtTokenProvider.getRole()).thenReturn(Role.USER);
        Mockito.when(mockJwtTokenProvider.getUsername()).thenReturn("user");

        Assertions.assertDoesNotThrow(() -> userLimitsService.getUserLimits(1));
    }

    @Test
    void getUserOfOtherUserThrowsAuthenticationException() {
        Mockito.when(userLimitsRepository.findFirstByUserId(Mockito.anyInt())).thenReturn(limits);
        Mockito.when(transactionRepository.findAllByTimestampIsAfterAndUserId(Mockito.any(), Mockito.anyInt())).thenReturn(java.util.List.of(sendTransaction, receiveTransaction, withdrawal, deposit));

        Mockito.when(userRepository.findById(Mockito.anyInt())).thenReturn(java.util.Optional.of(user));
        Mockito.when(mockJwtTokenProvider.getRole()).thenReturn(Role.USER);
        Mockito.when(mockJwtTokenProvider.getUsername()).thenReturn("user2");

        Assertions.assertThrows(AuthenticationException.class, () -> userLimitsService.getUserLimits(1));
    }

    @Test
    void getUserLimitsOfUserThatHasNoAccountsThrowsMethodNotAllowedException() {
        Mockito.when(userLimitsRepository.findFirstByUserId(Mockito.anyInt())).thenReturn(limits);
        Mockito.when(transactionRepository.findAllByTimestampIsAfterAndUserId(Mockito.any(), Mockito.anyInt())).thenReturn(java.util.List.of(sendTransaction, receiveTransaction, withdrawal, deposit));

        Mockito.when(userRepository.findById(Mockito.anyInt())).thenReturn(java.util.Optional.of(userWithNoAccount));
        Mockito.when(mockJwtTokenProvider.getRole()).thenReturn(Role.ADMIN);
        Mockito.when(mockJwtTokenProvider.getUsername()).thenReturn("user");

        Assertions.assertThrows(MethodNotAllowedException.class, () -> userLimitsService.getUserLimits(3));
    }

    @Test
    void initialiseLimitsShouldWork() {
        Assertions.assertDoesNotThrow(() -> userLimitsService.initialiseLimits(user));
    }

    @Test
    void updatingUserLimitsShouldReturnLimits() throws AuthenticationException {
        Mockito.when(userRepository.findById(Mockito.anyInt())).thenReturn(java.util.Optional.of(userWithNoAccount));
        Mockito.when(mockJwtTokenProvider.getRole()).thenReturn(Role.ADMIN);
        Mockito.when(mockJwtTokenProvider.getUsername()).thenReturn("user");

        Mockito.when(userLimitsRepository.findFirstByUserId(Mockito.anyInt())).thenReturn(limits);

        Limits limits = userLimitsService.updateUserLimits(1, userLimitsRequest);
        Assertions.assertEquals(1000, limits.getTransactionLimit());
    }

    @Test
    void updatingUserLimitsAsUserThrowsAuthenticationException() {
        Mockito.when(userRepository.findById(Mockito.anyInt())).thenReturn(java.util.Optional.of(userWithNoAccount));
        Mockito.when(mockJwtTokenProvider.getRole()).thenReturn(Role.USER);
        Mockito.when(mockJwtTokenProvider.getUsername()).thenReturn("user");

        Mockito.when(userLimitsRepository.findFirstByUserId(Mockito.anyInt())).thenReturn(limits);

        Assertions.assertThrows(AuthenticationException.class, () -> userLimitsService.updateUserLimits(1, userLimitsRequest));
    }
}
