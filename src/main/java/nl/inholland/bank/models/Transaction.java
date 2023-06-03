package nl.inholland.bank.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue
    @Column
    private int id;

    @Column
    private LocalDateTime timestamp;

    @ManyToOne
    @PrimaryKeyJoinColumn
    private User user;

    @OneToOne
    @Nullable
    private Account accountSender;

    @OneToOne
    @Nullable
    private Account accountReceiver;

    @Column
    private double amount;

    @Column
    private CurrencyType currencyType;

    @Column
    private TransactionType transactionType;

    @Column
    private String description;
    
    public Transaction(User user, @Nullable Account accountSender, @Nullable Account accountReceiver, double amount, CurrencyType currencyType, TransactionType transactionType) {
        this.timestamp = LocalDateTime.now();
        this.user = user;
        this.accountSender = accountSender;
        this.accountReceiver = accountReceiver;
        this.amount = amount;
        this.currencyType = currencyType;
        this.transactionType = transactionType;
    }

    public void setAmount(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount cannot be negative or zero.");
        }

        this.amount = amount;
    }

    public void setCurrencyType(CurrencyType currencyType) {
        if (currencyType == null) {
            throw new IllegalArgumentException("Currency type cannot be null.");
        }

        this.currencyType = currencyType;
    }

    public void setTransactionType(TransactionType transactionType) {
        if (transactionType == null) {
            throw new IllegalArgumentException("Transaction type cannot be null.");
        }

        this.transactionType = transactionType;
    }

    public void setAccountSender(Account accountSender) {
        this.accountSender = accountSender;
    }

    public void setAccountReceiver(Account accountReceiver) {
        this.accountReceiver = accountReceiver;
    }

    public void setUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }

        this.user = user;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp cannot be null.");
        }
        if (timestamp.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Timestamp cannot be in the future.");
        }
        this.timestamp = timestamp;
    }
}
