package nl.inholland.bank.repositories;

import nl.inholland.bank.models.BlacklistedRefreshToken;
import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenBlacklistRepository extends CrudRepository<BlacklistedRefreshToken, String> {
    /**
     * Checks if a token is blacklisted
     * @param token The token to check
     * @return True if the token is blacklisted, false otherwise
     */
    boolean existsByToken(String token);
}
