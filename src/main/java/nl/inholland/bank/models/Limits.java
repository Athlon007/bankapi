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
    private int transactionLimit;
    private int dailyTransactionLimit;
    private int absoluteLimit; // Minimum balance
    @Transient
    // Calculated by the service
    private double remainingDailyTransactionLimit;
}
