package nl.inholland.bank.models.dtos.AccountDTO;

public record AccountClientResponse(int id, String IBAN, String currency_type, String account_type,
                                    boolean isActive, String firstName, String lastName) {
}
