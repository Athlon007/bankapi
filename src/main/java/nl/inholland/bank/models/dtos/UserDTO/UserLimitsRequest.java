package nl.inholland.bank.models.dtos.UserDTO;

public record UserLimitsRequest(double transaction_limit, double daily_transaction_limit) {
}
