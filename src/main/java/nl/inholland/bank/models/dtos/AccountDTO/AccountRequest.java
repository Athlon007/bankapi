package nl.inholland.bank.models.dtos.AccountDTO;

public record AccountRequest(String IBAN, double balance, String currencyType, String accountType, String userId) {
}
