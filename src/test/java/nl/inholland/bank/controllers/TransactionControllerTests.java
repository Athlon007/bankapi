package nl.inholland.bank.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.inholland.bank.configuration.ApiTestConfiguration;
import nl.inholland.bank.models.*;
import nl.inholland.bank.models.dtos.AccountDTO.AccountClientResponse;
import nl.inholland.bank.models.dtos.TransactionDTO.TransactionRequest;
import nl.inholland.bank.models.dtos.TransactionDTO.TransactionResponse;
import nl.inholland.bank.models.dtos.TransactionDTO.TransactionSearchRequest;
import nl.inholland.bank.models.dtos.TransactionDTO.WithdrawDepositRequest;
import nl.inholland.bank.models.exceptions.UserNotTheOwnerOfAccountException;
import nl.inholland.bank.services.TransactionService;
import nl.inholland.bank.services.UserService;
import org.hibernate.ObjectNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.client.HttpClientErrorException;

import javax.naming.InsufficientResourcesException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.sasl.AuthenticationException;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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

    private Transaction mockTransaction;

    ObjectMapper mapper = new ObjectMapper();

    WithdrawDepositRequest mockWithdrawDepositRequest;

    private TransactionRequest mockTransactionRequest;

    private User mockUser = new User();

    private Account mockAccountFrom;
    private Account mockAccountTo;

    private WithdrawDepositRequest mockBadTransactionRequest;

    @BeforeEach
    void setUp() {
        mockAccountFrom = new Account();
        mockAccountFrom.setId(1);
        mockAccountFrom.setBalance(150);
        mockAccountFrom.setIBAN("NL11INHO6847043768");
        mockAccountFrom.setType(AccountType.CURRENT);
        mockAccountFrom.setUser(mockUser);

        mockAccountTo = new Account();
        mockAccountTo.setId(2);
        mockAccountTo.setBalance(100);
        mockAccountTo.setIBAN("NL34INHO3870387379");

        mockUser = new User();
        mockUser.setId(1);
        mockUser.setFirstName("user");
        mockUser.setLastName("test");
        mockUser.setCurrentAccount(mockAccountFrom);

        mockTransaction = new Transaction();
        mockTransaction.setId(1);
        mockTransaction.setAmount(100);
        mockTransaction.setAccountReceiver(mockAccountTo);
        mockTransaction.setAccountSender(mockAccountFrom);
        mockTransaction.setUser(mockUser);

        Transaction mockTransaction2 = new Transaction();
        mockTransaction2.setId(2);
        mockTransaction2.setAmount(100);
        mockTransaction2.setAccountReceiver(mockAccountTo);
        mockTransaction2.setAccountSender(mockAccountFrom);

        mockTransactionRequest = new TransactionRequest("NL37INHO4953307353", "NL77INHO6916372942", 100, "testing");
        mockWithdrawDepositRequest = new WithdrawDepositRequest("NL37INHO4953307353", 100, CurrencyType.EURO);
        mockBadTransactionRequest = new WithdrawDepositRequest("NL01INH12345678902qwrzhuzsgvbhSGLZCadblvaghvlizgsLZIVG", 100, CurrencyType.EURO);
    }



    @Test
    void transferMoneyWithUnknownAccountOrOwnerShouldReturnCodeFourHundred() throws Exception {
        when(transactionService.transferMoney(new User(), new Account(), new Account(), CurrencyType.EURO, 100, "testing")).thenReturn(mockTransaction);
        when(userService.getBearerUserRole()).thenReturn(Role.ADMIN);
        mockMvc.perform(MockMvcRequestBuilders.post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }


    @Test
    void transferMoneyShouldBeSuccessfulAndReturn201() throws Exception {
        Mockito.when(transactionService.processTransaction(
                ArgumentMatchers.any(TransactionRequest.class)
        )).thenReturn(mockTransaction);

        Mockito.when(userService.getBearerUserRole()).thenReturn(Role.CUSTOMER);

        MockHttpServletRequestBuilder post = MockMvcRequestBuilders.post(
                "/transactions")
                .contentType("application/json")
                .content(mapper.writeValueAsString(mockTransactionRequest))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + "token");

        mockMvc.perform(post)
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.amount").value(100))
                .andExpect(MockMvcResultMatchers.jsonPath("$.sender_iban").value("NL11INHO6847043768"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.receiver_iban").value("NL34INHO3870387379"));
    }

    @Test
    void getAllTransactionsShouldReturnATransaction() throws Exception {
        when(transactionService.getTransactions(Optional.empty(), Optional.empty(), new TransactionSearchRequest(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())))
                .thenReturn(List.of(mockTransaction));

        Mockito.when(userService.getBearerUserRole()).thenReturn(Role.ADMIN);

        mockMvc.perform(MockMvcRequestBuilders.get("/transactions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].amount").value(100))
                .andExpect(jsonPath("$[0].sender_iban").value("NL11INHO6847043768"))
                .andExpect(jsonPath("$[0].receiver_iban").value("NL34INHO3870387379"));
    }

    @Test
    void depositMoneyShouldBeSuccessfulAndReturn201() throws Exception {
        when(transactionService.depositMoney(mockWithdrawDepositRequest)).thenReturn(mockTransaction);
        when(userService.getBearerUserRole()).thenReturn(Role.CUSTOMER);

        mockMvc.perform(MockMvcRequestBuilders.post("/transactions/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(mockWithdrawDepositRequest)))
                .andExpect(status().is(201))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(100))
                .andExpect(jsonPath("$.receiver_iban").value("NL34INHO3870387379"));
    }

    @Test
    void withdrawMoneyShouldBeSuccessfulAndReturn201() throws Exception {
        when(transactionService.withdrawMoney(mockWithdrawDepositRequest)).thenReturn(mockTransaction);
        when(userService.getBearerUserRole()).thenReturn(Role.CUSTOMER);

        mockMvc.perform(MockMvcRequestBuilders.post("/transactions/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(mockWithdrawDepositRequest)))
                .andExpect(status().is(201))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(100))
                .andExpect(jsonPath("$.sender_iban").value("NL11INHO6847043768"));
    }


//
//    @Test
//    @WithMockUser(username = "admin", roles = {"ADMIN"})
//    void getAllTransactionThrowingObjectNotFoundExceptionShouldResultInCodeFourHundred() throws Exception {
//        when(transactionService.getTransactions(Optional.empty(), Optional.empty(), new TransactionSearchRequest(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())))
//                .thenReturn(mockTransaction);
//
//        Mockito.when(userService.getBearerUserRole());
//        mockMvc.perform(MockMvcRequestBuilders.get("/transactions?ifuivizb=djviu")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().is(400));
//    }
//
//
//    @Test
//    @WithMockUser(username = "admin", roles = {"ADMIN"})
//    void depositMoney() throws Exception {
//        when(transactionService.depositMoney(withdrawDepositRequest)).thenReturn(mockTransaction.get(0));
//
//        Mockito.when(userService.getBearerUserRole()).thenReturn(Role.ADMIN);
//
//        MockHttpServletRequestBuilder post = MockMvcRequestBuilders.post("/transactions/deposit")
//                .contentType("application/json")
//                .content(mapper.writeValueAsString(withdrawDepositRequest))
//                .header(HttpHeaders.AUTHORIZATION, "Bearer " + "token");
//        mockMvc.perform(post)
//                .andExpect(MockMvcResultMatchers.status().isCreated());
//
//
//    }
//
//    @Test
//    @WithMockUser(username = "admin", roles = {"ADMIN"})
//    void withdrawMoneyShouldReturnATransactionResponseAndTwoOOne() throws Exception, UserNotTheOwnerOfAccountException {
//        when(transactionService.withdrawMoney(withdrawDepositRequest)).thenReturn(mockTransaction.get(0));
//
//        Mockito.when(userService.getBearerUserRole()).thenReturn(Role.ADMIN);
//
//        MockHttpServletRequestBuilder post = MockMvcRequestBuilders.post("/transactions/withdraw")
//                .contentType("application/json")
//                .content(mapper.writeValueAsString(withdrawDepositRequest))
//                .header(HttpHeaders.AUTHORIZATION, "Bearer " + "token");
//
//        mockMvc.perform(post)
//                .andExpect(MockMvcResultMatchers.status().isCreated());
//    }
//
//    @Test
//    void withdrawMoneyWithoutTokenShouldReturnUnauthenticatedStatus() throws Exception, UserNotTheOwnerOfAccountException {
//        when(transactionService.withdrawMoney(withdrawDepositRequest)).thenThrow(HttpClientErrorException.create(HttpStatus.UNAUTHORIZED, "Unauthorized", HttpHeaders.EMPTY, null, null));
//        //Mockito.when(userService.getBearerUserRole()).thenReturn(Role.ADMIN);
//
//
//        MockHttpServletRequestBuilder post = MockMvcRequestBuilders.post("/transactions/withdraw")
//                .contentType("application/json")
//                .content(mapper.writeValueAsString(withdrawDepositRequest));
//
//        mockMvc.perform(post)
//                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
//    }

    @Test
    void withdrawMoneyWithUnknownAccountShouldResultInFourOFour() throws Exception, UserNotTheOwnerOfAccountException {

        when(transactionService.withdrawMoney(mockBadTransactionRequest)).thenThrow(ObjectNotFoundException.class);

        Mockito.when(userService.getBearerUserRole()).thenReturn(Role.ADMIN);

        MockHttpServletRequestBuilder post = MockMvcRequestBuilders.post("/transactions/withdraw")
                .contentType("application/json")
                .content(mapper.writeValueAsString(mockBadTransactionRequest));

        mockMvc.perform(post)
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }


//    @Test
//    void withdrawMoneyWithBadRequestShouldResultInFiveHundred() throws Exception, UserNotTheOwnerOfAccountException {
//
//
//        when(transactionService.withdrawMoney(mockBadTransactionRequest)).thenReturn(mockTransaction.get(0));
//
//        Mockito.when(userService.getBearerUserRole()).thenReturn(Role.ADMIN);
//
//        MockHttpServletRequestBuilder post = MockMvcRequestBuilders.post("/transactions/withdraw")
//                .contentType("application/json")
//                .content(mapper.writeValueAsString(withdrawDepositRequest));
//
//        mockMvc.perform(post)
//                .andExpect(MockMvcResultMatchers.status().is(500));
//    }

}
