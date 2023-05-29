package nl.inholland.bank.models.dtos.TransactionDTO;

import java.time.LocalDateTime;
import java.util.Optional;

public record TransactionSearchRequest(Optional<Double> minAmount, Optional<Double> maxAmount,
                                       Optional<LocalDateTime> startDate, Optional<LocalDateTime> endDate,
                                       Optional<String> ibanSender, Optional<String> ibanReceiver,
                                       Optional<Integer> userSenderId, Optional<Integer> userReceiverId) {
}
