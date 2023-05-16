package nl.inholland.bank.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Account {
    @Id
    private int id;
    @ManyToOne
    @JsonIgnore
    private User user;
}
