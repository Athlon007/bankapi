package nl.inholland.bank.models.dtos.TransactionDTO;

import nl.inholland.bank.models.CurrencyType;

public record WithdrawRequest(String IBAN, double amount, CurrencyType currencyType) {
}
