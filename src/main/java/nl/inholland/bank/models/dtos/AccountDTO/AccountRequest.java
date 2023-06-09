package nl.inholland.bank.models.dtos.AccountDTO;

public record AccountRequest(String currencyType, String accountType, int userId) {
}
