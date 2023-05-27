package nl.inholland.bank.models;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue
    @Column
    private int id;

    @Column
    private LocalDate timestamp;

    @OneToOne
    @PrimaryKeyJoinColumn
    private User user;

    @OneToOne
    @Nullable
    private Account AccountSender;

    @OneToOne
    @Nullable
    private Account AccountReceiver;

    @Column
    private double amount;

    @Column
    private CurrencyType currencyType;

    @Column
    private TransactionType transactionType;

    public Transaction(User user, @Nullable Account AccountSender, @Nullable Account AccountReceiver, double amount, CurrencyType currencyType, TransactionType transactionType) {
        this.timestamp = LocalDate.now();
        this.user = user;
        this.AccountSender = AccountSender;
        this.AccountReceiver = AccountReceiver;
        this.amount = amount;
        this.currencyType = currencyType;
        this.transactionType = transactionType;
    }

    public void setAmount(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount cannot be negative or zero");
        }

        this.amount = amount;
    }

    public void setCurrencyType(CurrencyType currencyType) {
        if (currencyType == null) {
            throw new IllegalArgumentException("Currency type cannot be null");
        }

        this.currencyType = currencyType;
    }

    public void setTransactionType(TransactionType transactionType) {
        if (transactionType == null) {
            throw new IllegalArgumentException("Transaction type cannot be null");
        }

        this.transactionType = transactionType;
    }

    public void setAccountSender(Account AccountSender) {
        if (AccountSender == null) {
            throw new IllegalArgumentException("Account sender cannot be null");
        }

        this.AccountSender = AccountSender;
    }

    public void setAccountReceiver(Account AccountReceiver) {
        if (AccountReceiver == null) {
            throw new IllegalArgumentException("Account receiver cannot be null");
        }

        this.AccountReceiver = AccountReceiver;
    }

    public void setUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        this.user = user;
    }

    public void setTimestamp(LocalDate timestamp) {
        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp cannot be null");
        }
        if (timestamp.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Timestamp cannot be in the future");
        }
        this.timestamp = timestamp;
    }
}
