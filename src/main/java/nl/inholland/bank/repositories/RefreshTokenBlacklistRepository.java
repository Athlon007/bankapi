package nl.inholland.bank.repositories;

import nl.inholland.bank.models.BlacklistedRefreshToken;
import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenBlacklistRepository extends CrudRepository<BlacklistedRefreshToken, String> {
    boolean existsByToken(String token);
}
