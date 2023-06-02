package nl.inholland.bank.models.dtos.AccountDTO;

public record AccountResponse(int id, String IBAN, String currency_type, String account_type, boolean isActive, double balance) {

}
