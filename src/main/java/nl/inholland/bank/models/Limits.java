package nl.inholland.bank.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public class Limits {
    @Id
    @GeneratedValue
    private Integer id;
    private double transactionLimit;
    private double dailyTransactionLimit;
    private double absoluteLimit; // Minimum balance
    @Transient
    // Calculated by the service
    private double remainingDailyTransactionLimit;
}
