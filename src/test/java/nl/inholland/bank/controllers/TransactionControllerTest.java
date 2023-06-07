package nl.inholland.bank.controllers;

import nl.inholland.bank.configuration.ApiTestConfiguration;
import nl.inholland.bank.models.Account;
import nl.inholland.bank.models.Role;
import nl.inholland.bank.models.Transaction;
import nl.inholland.bank.models.dtos.TransactionDTO.TransactionSearchRequest;
import nl.inholland.bank.models.dtos.TransactionDTO.WithdrawDepositRequest;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

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

    private List<Transaction> mockTransaction;

    private Account mockAccountFrom;
    private Account mockAccountTo;

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


    }

    @Test
    void transferMoney() {
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
    void depositMoney() throws Exeption{
        when(transactionService.depositMoney(1, 100)).thenReturn(mockTransaction.get(0));

        mockMvc.perform(MockMvcRequestBuilders.post("/transactions/deposit/1/100")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(100))
                .andExpect(jsonPath("$.accountReceiver.id").value(2))
                .andExpect(jsonPath("$.accountSender.id").value(1));
    }
    @Test
    void withdrawMoney() throws Exception{
        when(transactionService.withdrawMoney(new WithdrawDepositRequest())).thenReturn(mockTransaction.get(0));

        mockMvc.perform(MockMvcRequestBuilders.post("/transactions/withdraw/1/100")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(100))
                .andExpect(jsonPath("$.accountReceiver.id").value(2))
                .andExpect(jsonPath("$.accountSender.id").value(1));
    }
    @Test
    void buildTransactionResponse() {
    }
}