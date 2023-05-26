package nl.inholland.bank.models.dtos.AccountDTO;

public record AccountResponse(int id, String IBAN, String account_type, String currency_type, double balance) {

}
