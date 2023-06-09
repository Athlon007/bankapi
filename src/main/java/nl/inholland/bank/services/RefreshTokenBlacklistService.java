package nl.inholland.bank.services;

import nl.inholland.bank.models.BlacklistedRefreshToken;
import nl.inholland.bank.repositories.RefreshTokenBlacklistRepository;
import org.springframework.stereotype.Service;

/**
 * Service for managing the refresh token blacklist
 */
@Service
public class RefreshTokenBlacklistService {
    private final RefreshTokenBlacklistRepository refreshTokenBlacklistRepository;

    public RefreshTokenBlacklistService(RefreshTokenBlacklistRepository refreshTokenBlacklistRepository) {
        this.refreshTokenBlacklistRepository = refreshTokenBlacklistRepository;
    }

    /**
     * Checks if a token is blacklisted
     * @param token The token to check
     * @return True if the token is blacklisted, false otherwise
     */
    public boolean isBlacklisted(String token) {
        return refreshTokenBlacklistRepository.existsByToken(token);
    }

    /**
     * Blacklists a token
     * @param token The token to blacklist
     */
    public void blacklist(String token) {
        if (!isBlacklisted(token))
            refreshTokenBlacklistRepository.save(new BlacklistedRefreshToken(token));
    }
}
