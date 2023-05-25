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
}
