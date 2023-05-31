package nl.inholland.bank.models.dtos.TransactionDTO;

import nl.inholland.bank.models.TransactionType;

import java.time.LocalDateTime;

public record TransactionResponse(long id, String sender_iban, String receiver_iban, double amount, LocalDateTime timestamp, String description, TransactionType transactionType) {
}
