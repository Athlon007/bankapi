package nl.inholland.bank.models.dtos;

import nl.inholland.bank.models.CurrencyType;
import nl.inholland.bank.models.TransactionType;
import nl.inholland.bank.models.dtos.TransactionDTO.TransactionRequest;
import nl.inholland.bank.models.dtos.TransactionDTO.TransactionResponse;
import nl.inholland.bank.models.dtos.TransactionDTO.TransactionSearchRequest;
import nl.inholland.bank.models.dtos.TransactionDTO.WithdrawDepositRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

class TransactionDTOsTests {
    @Test
    void transactionRequest() {
        TransactionRequest transactionRequest = new TransactionRequest("senderiban", "reiceiveriban", 100, "description");

        Assertions.assertEquals("senderiban", transactionRequest.sender_iban());
        Assertions.assertEquals("reiceiveriban", transactionRequest.receiver_iban());
        Assertions.assertEquals(100, transactionRequest.amount());
        Assertions.assertEquals("description", transactionRequest.description());
    }

    @Test
    void transactionResponse() {
        TransactionResponse transactionResponse = new TransactionResponse(1, "username", "senderiban", "reiceiveriban", 100, CurrencyType.EURO, LocalDateTime.of(2023, 6, 10, 18, 25, 0), "description", TransactionType.TRANSACTION);

        Assertions.assertEquals(1, transactionResponse.id());
        Assertions.assertEquals("username", transactionResponse.username());
        Assertions.assertEquals("senderiban", transactionResponse.sender_iban());
        Assertions.assertEquals("reiceiveriban", transactionResponse.receiver_iban());
        Assertions.assertEquals(100, transactionResponse.amount());
        Assertions.assertEquals(CurrencyType.EURO, transactionResponse.currencyType());
        Assertions.assertEquals(LocalDateTime.of(2023, 6, 10, 18, 25, 0), transactionResponse.timestamp());
        Assertions.assertEquals("description", transactionResponse.description());
        Assertions.assertEquals(TransactionType.TRANSACTION, transactionResponse.transactionType());

    }

    @Test
    void transactionSearchRequest() {
        /*record TransactionSearchRequest(Optional<Double> minAmount, Optional<Double> maxAmount,
                               Optional<LocalDateTime> startDate, Optional<LocalDateTime> endDate,
                               Optional<Integer> transactionID,
                               Optional<String> ibanSender, Optional<String> ibanReceiver,
                               Optional<Integer> userSenderId, Optional<Integer> userReceiverId,
                               Optional<String> transactionType)*/
        TransactionSearchRequest transactionSearchRequest = new TransactionSearchRequest(Optional.of(0d), Optional.of(100d),
Optional.of(LocalDateTime.of(2023, 6, 10, 18, 25, 0)), Optional.of(LocalDateTime.of(2023, 6, 10, 18, 25, 0)),
                Optional.of(1), Optional.of("senderiban"), Optional.of("reiceiveriban"),
                Optional.of(1), Optional.of(1), Optional.of("transactionType"));

        Assertions.assertEquals(Optional.of(0d), transactionSearchRequest.minAmount());
        Assertions.assertEquals(Optional.of(100d), transactionSearchRequest.maxAmount());
        Assertions.assertEquals(Optional.of(LocalDateTime.of(2023, 6, 10, 18, 25, 0)), transactionSearchRequest.startDate());
        Assertions.assertEquals(Optional.of(LocalDateTime.of(2023, 6, 10, 18, 25, 0)), transactionSearchRequest.endDate());
        Assertions.assertEquals(Optional.of(1), transactionSearchRequest.transactionID());
        Assertions.assertEquals(Optional.of("senderiban"), transactionSearchRequest.ibanSender());
        Assertions.assertEquals(Optional.of("reiceiveriban"), transactionSearchRequest.ibanReceiver());
        Assertions.assertEquals(Optional.of(1), transactionSearchRequest.userSenderId());
        Assertions.assertEquals(Optional.of(1), transactionSearchRequest.userReceiverId());
        Assertions.assertEquals(Optional.of("transactionType"), transactionSearchRequest.transactionType());
    }

    @Test
    void withdrawDepositRequest() {
        WithdrawDepositRequest withdrawDepositRequest = new WithdrawDepositRequest("iban", 100, CurrencyType.EURO);

        Assertions.assertEquals("iban", withdrawDepositRequest.IBAN());
        Assertions.assertEquals(100, withdrawDepositRequest.amount());
        Assertions.assertEquals(CurrencyType.EURO, withdrawDepositRequest.currencyType());
    }
}
