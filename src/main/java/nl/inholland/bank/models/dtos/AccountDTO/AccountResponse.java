package nl.inholland.bank.models.dtos.AccountDTO;

import org.iban4j.Iban;

public record AccountResponse(int id, String IBAN, String currency_type, String account_type, boolean isActive, double balance) {

}
