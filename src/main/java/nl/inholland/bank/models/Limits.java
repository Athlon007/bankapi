package nl.inholland.bank.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Data
@Entity
@NoArgsConstructor
public class Limits {
    @Id
    @GeneratedValue
    private Integer id;
    private double transactionLimit;
    private double dailyTransactionLimit;
    @Transient
    // Calculated by the service
    private double remainingDailyTransactionLimit;

    @ToString.Exclude
    @JsonIgnore
    @OneToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    public void setTransactionLimit(double transactionLimit) {
        if (transactionLimit < 0) {
            throw new IllegalArgumentException("Transaction limit cannot be lower than 0");
        }
        this.transactionLimit = transactionLimit;
    }

    public void setDailyTransactionLimit(double dailyTransactionLimit) {
        if (dailyTransactionLimit < 0) {
            throw new IllegalArgumentException("Daily transaction limit cannot be lower than 0");
        }
        this.dailyTransactionLimit = dailyTransactionLimit;
    }

    public void setRemainingDailyTransactionLimit(double remainingDailyTransactionLimit) {
        if (remainingDailyTransactionLimit < 0) {
            remainingDailyTransactionLimit = 0; // Can't be lower than 0.
        }
        this.remainingDailyTransactionLimit = remainingDailyTransactionLimit;
    }
}
