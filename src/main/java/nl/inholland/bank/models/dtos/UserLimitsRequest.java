package nl.inholland.bank.models.dtos;

public record UserLimitsRequest(double transaction_limit, double daily_transaction_limit, double absolute_limit) {
}
