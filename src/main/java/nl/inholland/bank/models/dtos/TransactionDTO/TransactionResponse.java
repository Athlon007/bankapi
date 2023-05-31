package nl.inholland.bank.models.dtos.TransactionDTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import nl.inholland.bank.models.TransactionType;

import java.time.LocalDateTime;

public record TransactionResponse(long id, String username, @JsonInclude(JsonInclude.Include.NON_NULL) String sender_iban,@JsonInclude(JsonInclude.Include.NON_NULL)
String receiver_iban, double amount, LocalDateTime timestamp, String description, TransactionType transactionType) {
}
