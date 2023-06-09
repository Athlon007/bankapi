package nl.inholland.bank.models.dtos.UserDTO;

public record UserLimitsResponse(double transaction_limit, double daily_transaction_limit, double absolute_limit, double remaining_daily_transaction_limit) {
}
