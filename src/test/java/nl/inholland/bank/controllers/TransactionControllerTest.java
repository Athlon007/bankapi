package nl.inholland.bank.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.inholland.bank.configuration.ApiTestConfiguration;
import nl.inholland.bank.models.*;
import nl.inholland.bank.models.dtos.TransactionDTO.TransactionRequest;
import nl.inholland.bank.models.dtos.TransactionDTO.TransactionResponse;
import nl.inholland.bank.models.dtos.TransactionDTO.TransactionSearchRequest;
import nl.inholland.bank.models.dtos.TransactionDTO.WithdrawDepositRequest;
import nl.inholland.bank.models.exceptions.UserNotTheOwnerOfAccountException;
import nl.inholland.bank.services.TransactionService;
import nl.inholland.bank.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.naming.InsufficientResourcesException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.sasl.AuthenticationException;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(SpringExtension.class)
@WebMvcTest(TransactionController.class)
@Import(ApiTestConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    TransactionService transactionService;

    @MockBean
    UserService userService;


    ObjectMapper mapper = new ObjectMapper();

    WithdrawDepositRequest withdrawDepositRequest;

    private User mockUser;

    private List<Transaction> mockTransaction;

    private Account mockAccountFrom;
    private Account mockAccountTo;

    private TransactionRequest mockTransactionRequest;

    private WithdrawDepositRequest mockBadTransactionRequest;


    @BeforeEach
    void setUp() {
        mockAccountFrom = new Account();
        mockAccountFrom.setId(1);
        mockAccountFrom.setBalance(100);
        mockAccountFrom.setIBAN("NL01INHO0000000001");

        mockAccountTo = new Account();
        mockAccountTo.setId(2);
        mockAccountTo.setBalance(100);
        mockAccountTo.setIBAN("NL01INHO0000000002");

        mockUser = new User();
        mockUser.setId(1);
        mockUser.setFirstName("test");
        mockUser.setLastName("test");
        mockUser.setRole(Role.ADMIN);

        mockAccountFrom.setUser(mockUser);




        Transaction mockTransaction = new Transaction();
        mockTransaction.setId(1);
        mockTransaction.setAmount(100);
        mockTransaction.setAccountReceiver(mockAccountTo);
        mockTransaction.setAccountSender(mockAccountFrom);

        Transaction mockTransaction2 = new Transaction();
        mockTransaction2.setId(2);
        mockTransaction2.setAmount(100);
        mockTransaction2.setAccountReceiver(mockAccountTo);
        mockTransaction2.setAccountSender(mockAccountFrom);

        this.mockTransaction = List.of(
                mockTransaction,
                mockTransaction2
        );

        mockTransactionRequest = new TransactionRequest("NL01INHO0000000001", "NL01INHO0000000002", 100, "testing");
        withdrawDepositRequest = new WithdrawDepositRequest( "NL01INHO0000000001", 100, CurrencyType.EURO, mockUser.getId());
        mockBadTransactionRequest = new WithdrawDepositRequest( "NL01INH12345678902qwrzhuzsgvbhSGLZCadblvaghvlizgsLZIVG", 100, CurrencyType.EURO, 123456789);

    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void transferMoneyWithUnknownAccountOrOwnerShouldReturnCodeFourHundred() throws Exception {
        when(transactionService.transferMoney(new User(), new Account(), new Account(), CurrencyType.EURO, 100, "testing")).thenReturn(mockTransaction.get(0));

        Mockito.when(userService.getBearerUserRole()).thenReturn(Role.ADMIN);

        mockMvc.perform(MockMvcRequestBuilders.post("/transactions")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    @WithMockUser
    void transferMoneyShouldReturnATransactionResponse() throws Exception, UserNotTheOwnerOfAccountException {
        when(transactionService.processTransaction(mockTransactionRequest)).thenReturn(mockTransaction.get(0));

        Mockito.when(userService.getBearerUserRole()).thenReturn(Role.ADMIN);

        MockHttpServletRequestBuilder post = MockMvcRequestBuilders.post("/transactions")
                        .contentType("application/json")
                                .content(mapper.writeValueAsString(mockTransactionRequest))
                                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + "token");

        mockMvc.perform(post)
                        .andExpect(MockMvcResultMatchers.status().isCreated());
    }
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getAllTransactionsShouldReturnAListOfOneTransaction() throws Exception {
        when(transactionService.getTransactions(Optional.empty(), Optional.empty(), new TransactionSearchRequest(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())))
                .thenReturn(mockTransaction);

        Mockito.when(userService.getBearerUserRole()).thenReturn(Role.ADMIN);

        mockMvc.perform(MockMvcRequestBuilders.get("/transactions")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getAllTransactionThrowingObjectNotFoundExceptionShouldResultInCodeFourHundred() throws Exception {
        when(transactionService.getTransactions(Optional.empty(), Optional.empty(), new TransactionSearchRequest(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())))
                .thenReturn(mockTransaction);

        Mockito.when(userService.getBearerUserRole());
        mockMvc.perform(MockMvcRequestBuilders.get("/transactions?ifuivizb=djviu")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }



    @Test
    void depositMoney() throws Exeption{

    }
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void withdrawMoneyShouldReturnATransactionResponseAndTwoOOne() throws Exception, UserNotTheOwnerOfAccountException {
        when(transactionService.withdrawMoney(withdrawDepositRequest)).thenReturn(mockTransaction.get(0));

        Mockito.when(userService.getBearerUserRole()).thenReturn(Role.ADMIN);

        MockHttpServletRequestBuilder post = MockMvcRequestBuilders.post("/transactions/withdraw")
                .contentType("application/json")
                .content(mapper.writeValueAsString(withdrawDepositRequest))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + "token");

        mockMvc.perform(post)
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    void withdrawMoneyWithoutTokenShouldReturnUnauthenticatedStatus() throws Exception, UserNotTheOwnerOfAccountException {
        when(transactionService.withdrawMoney(withdrawDepositRequest)).thenReturn(mockTransaction.get(0));
        //Mockito.when(userService.getBearerUserRole()).thenReturn(Role.ADMIN);


        MockHttpServletRequestBuilder post = MockMvcRequestBuilders.post("/transactions/withdraw")
                .contentType("application/json")
                .content(mapper.writeValueAsString(withdrawDepositRequest));

        mockMvc.perform(post)
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    void withdrawMoneyWithUnknownAccountShouldResultInFourOFour() throws Exception, UserNotTheOwnerOfAccountException {

        //BUSINESS CODE IS FAILING HERE
        when(transactionService.withdrawMoney(mockBadTransactionRequest)).thenReturn(mockTransaction.get(0));

        Mockito.when(userService.getBearerUserRole()).thenReturn(Role.ADMIN);

        MockHttpServletRequestBuilder post = MockMvcRequestBuilders.post("/transactions/withdraw")
                .contentType("application/json")
                .content(mapper.writeValueAsString(mockBadTransactionRequest));

        mockMvc.perform(post)
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void withdrawMoneyWithBadRequestShouldResultInFiveHundred() throws Exception, UserNotTheOwnerOfAccountException {


        when(transactionService.withdrawMoney(mockBadTransactionRequest)).thenReturn(mockTransaction.get(0));

        Mockito.when(userService.getBearerUserRole()).thenReturn(Role.ADMIN);

        MockHttpServletRequestBuilder post = MockMvcRequestBuilders.post("/transactions/withdraw")
                .contentType("application/json")
                .content(mapper.writeValueAsString(withdrawDepositRequest));

        mockMvc.perform(post)
                .andExpect(MockMvcResultMatchers.status().is(500));
    }





    @Test
    void buildTransactionResponse() {
    }
}