package nl.inholland.bank.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.iban4j.Iban;

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
    private double absoluteLimit;

    public Account(User user, double balance, CurrencyType currencyType, String IBAN, AccountType type, double absoluteLimit) {
        this.user = user;
        this.balance = balance;
        this.currencyType = currencyType;
        this.IBAN = IBAN;
        this.type = type;
        this.isActive = true;
        this.absoluteLimit = absoluteLimit;
    }

    public void setBalance(double balance) {
        if (Double.isNaN(balance)) {
            throw new IllegalArgumentException("Balance cannot be NaN");
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
        if (IBAN == null) {
            throw new IllegalArgumentException("IBAN cannot be null");
        }

        if (!IBANGenerator.isValidIBAN(IBAN)) {
            throw new IllegalArgumentException("IBAN is not valid");
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

    public void setAbsoluteLimit(double absoluteLimit) {
        if (absoluteLimit > 0) {
            throw new IllegalArgumentException("Absolute limit cannot be higher than 0");
        }
        this.absoluteLimit = absoluteLimit;
    }
}
