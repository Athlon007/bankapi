package nl.inholland.bank.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

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

    @ToString.Exclude
    @JsonIgnore
    @OneToOne
    private User user;
}
