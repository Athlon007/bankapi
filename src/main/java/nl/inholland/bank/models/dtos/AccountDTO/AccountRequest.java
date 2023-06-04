package nl.inholland.bank.models.dtos.AccountDTO;

import lombok.AllArgsConstructor;
import lombok.Data;

public record AccountRequest(String currencyType, String accountType, int userId) {
}
