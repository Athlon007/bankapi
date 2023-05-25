package nl.inholland.bank.models.dtos;

public record UserLimitsRequest(int transaction_limit, int daily_transaction_limit, int absolute_limit) {
}
