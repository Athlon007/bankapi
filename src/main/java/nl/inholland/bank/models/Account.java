package nl.inholland.bank.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Account {
    @Id
    private int id;
    @ManyToOne
    @JsonIgnore
    private User user;
    private double balance;
    private CurrencyType currencyType;
    private String IBAN;
    private AccountType type;
    private boolean isActive;
}
