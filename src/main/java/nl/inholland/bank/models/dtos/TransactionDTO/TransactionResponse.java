package nl.inholland.bank.models.dtos.TransactionDTO;

import java.time.LocalDate;

public record TransactionResponse(long id, String sender_iban, String receiver_iban, double amount, LocalDate timestamp, String description) {
}
