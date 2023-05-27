package nl.inholland.bank.models.dtos.TransactionDTO;

public record TransactionRequest(String sender_iban, String receiver_iban, double amount, String description) {
}
