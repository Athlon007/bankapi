package nl.inholland.bank.services;

import nl.inholland.bank.configuration.ApiTestConfiguration;
import nl.inholland.bank.repositories.RefreshTokenBlacklistRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import(ApiTestConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
public class RefreshTokenBlacklistServiceTests {
    private RefreshTokenBlacklistService refreshTokenBlacklistService;

    @MockBean
    private RefreshTokenBlacklistRepository refreshTokenBlacklistRepository;

    @BeforeEach
    void setUp() {
        refreshTokenBlacklistService = new RefreshTokenBlacklistService(refreshTokenBlacklistRepository);
    }

    @Test
    void isBlacklistedReturnsTrueWhenTokenIsBlacklisted() {
        Mockito.when(refreshTokenBlacklistRepository.existsByToken("token")).thenReturn(true);
        Assertions.assertTrue(refreshTokenBlacklistService.isBlacklisted("token"));
    }

    @Test
    void blacklistingTokenShouldSucceed() {
        Mockito.when(refreshTokenBlacklistRepository.existsByToken("token")).thenReturn(false);
        refreshTokenBlacklistService.blacklist("token");
        Mockito.verify(refreshTokenBlacklistRepository, Mockito.times(1)).save(Mockito.any());
    }
}
