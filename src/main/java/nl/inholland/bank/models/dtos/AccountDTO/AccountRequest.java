package nl.inholland.bank.models.dtos.AccountDTO;

public record AccountRequest(String IBAN, String currencyType, String accountType, String userId) {
}
