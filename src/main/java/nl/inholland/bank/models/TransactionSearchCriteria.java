package nl.inholland.bank.models;

import java.time.LocalDateTime;

public class TransactionSearchCriteria {
    private Double minAmount;
    private Double maxAmount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String ibanSender;
    private String ibanReceiver;
}
