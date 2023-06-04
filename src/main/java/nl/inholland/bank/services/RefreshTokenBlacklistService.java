package nl.inholland.bank.services;

import nl.inholland.bank.models.BlacklistedRefreshToken;
import nl.inholland.bank.repositories.RefreshTokenBlacklistRepository;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenBlacklistService {
    private final RefreshTokenBlacklistRepository refreshTokenBlacklistRepository;

    public RefreshTokenBlacklistService(RefreshTokenBlacklistRepository refreshTokenBlacklistRepository) {
        this.refreshTokenBlacklistRepository = refreshTokenBlacklistRepository;
    }

    public boolean isBlacklisted(String token) {
        return refreshTokenBlacklistRepository.existsByToken(token);
    }

    public void blacklist(String token) {
        System.out.println("Blacklisting token: " + token);
        if (!isBlacklisted(token))
            refreshTokenBlacklistRepository.save(new BlacklistedRefreshToken(token));
    }
}
