package nl.inholland.bank.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class BlacklistedRefreshToken {
    @Id
    @GeneratedValue
    private long id;
    @Column(length = 1000)
    private String token;

    public BlacklistedRefreshToken(String token) {
        this.token = token;
    }
}
