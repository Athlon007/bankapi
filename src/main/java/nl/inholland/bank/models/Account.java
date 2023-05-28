package nl.inholland.bank.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@NoArgsConstructor
public class Account {
    @Id
    @GeneratedValue
    private int id;
    @ManyToOne(cascade = CascadeType.ALL)
    @JsonIgnore
    @ToString.Exclude
    private User user;
    private double balance;
    private CurrencyType currencyType;
    @Column(unique = true)
    private String IBAN;
    private AccountType type;
    private boolean isActive;

    public Account(User user, double balance, CurrencyType currencyType, String IBAN, AccountType type) {
        this.user = user;
        this.balance = balance;
        this.currencyType = currencyType;
        this.IBAN = IBAN;
        this.type = type;
        this.isActive = true;
    }

    public void setBalance(double balance) {
        if (balance < 0) {
            throw new IllegalArgumentException("Balance cannot be negative");
        }

        this.balance = balance;
    }

    public void setCurrencyType(CurrencyType currencyType) {
        if (currencyType == null) {
            throw new IllegalArgumentException("Currency type cannot be null");
        }

        this.currencyType = currencyType;
    }

    public void setIBAN(String IBAN) {
        if (IBAN == null || IBAN.isEmpty()) {
            throw new IllegalArgumentException("IBAN cannot be null or empty");
        }

        this.IBAN = IBAN;
    }

    public void setType(AccountType type) {
        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null");
        }

        this.type = type;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }
}
