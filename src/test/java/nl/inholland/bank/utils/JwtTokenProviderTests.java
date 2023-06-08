package nl.inholland.bank.utils;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import nl.inholland.bank.configuration.ApiTestConfiguration;
import nl.inholland.bank.models.Role;
import nl.inholland.bank.models.User;
import nl.inholland.bank.models.dtos.Token;
import nl.inholland.bank.repositories.UserRepository;
import nl.inholland.bank.services.RefreshTokenBlacklistService;
import nl.inholland.bank.services.UserDetailsService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.naming.AuthenticationException;
import java.lang.reflect.Field;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@Import(ApiTestConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class JwtTokenProviderTests {
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private JwtKeyProvider jwtKeyProvider;

    @MockBean
    private RefreshTokenBlacklistService refreshTokenBlacklistService;

    @MockBean
    private UserRepository userRepository;

    private UserDetails userDetails;

    @BeforeEach
    public void setUp() {
        jwtTokenProvider = new JwtTokenProvider(userDetailsService, jwtKeyProvider, refreshTokenBlacklistService, userRepository);
        String key = "random_secret_key";
        Mockito.when(jwtKeyProvider.getPrivateKey()).thenReturn(getPasswordBasedKey(SignatureAlgorithm.HS256.getJcaName(), 256, key.toCharArray()));

        // reflection set utility for bankapi.token.expiration
        try {
            Field field = jwtTokenProvider.getClass().getDeclaredField("validityInMilliseconds");
            field.setAccessible(true);
            field.set(jwtTokenProvider, 2000);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        try {
            Field field = jwtTokenProvider.getClass().getDeclaredField("validityRefreshInMilliseconds");
            field.setAccessible(true);
            field.set(jwtTokenProvider, 2000);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }



        userDetails = new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return Collections.singleton(Role.USER);
            }

            @Override
            public String getPassword() {
                return "password";
            }

            @Override
            public String getUsername() {
                return "username";
            }

            @Override
            public boolean isAccountNonExpired() {
                return false;
            }

            @Override
            public boolean isAccountNonLocked() {
                return false;
            }

            @Override
            public boolean isCredentialsNonExpired() {
                return false;
            }

            @Override
            public boolean isEnabled() {
                return false;
            }
        };
    }

    private static Key getPasswordBasedKey(String cipher, int keySize, char[] password) {
        try {
            byte[] salt = new byte[100];
            SecureRandom random = new SecureRandom();
            random.nextBytes(salt);
            PBEKeySpec pbeKeySpec = new PBEKeySpec(password, salt, 1000, keySize);
            SecretKey pbeKey = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(pbeKeySpec);
            return new SecretKeySpec(pbeKey.getEncoded(), cipher);
        }
        catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void createTokenShouldReturnToken() {
        Token token = jwtTokenProvider.createToken("username", Role.USER);
        Assertions.assertNotNull(token);
    }

    @Test
    void createRefreshTokenWorks() {
        String refreshToken = jwtTokenProvider.createRefreshToken("username");
        Assertions.assertNotNull(refreshToken);
    }

    @Test
    void getAuthenticationShouldReturnAuthentication() {
        Mockito.when(userDetailsService.loadUserByUsername("username")).thenReturn(userDetails);

        Token token = jwtTokenProvider.createToken("username", Role.USER);
        Authentication authentication = jwtTokenProvider.getAuthentication(token.jwt());
        Assertions.assertNotNull(authentication);
    }

    @Test
    void getAuthenticationForExpiredTokenShouldThrowRuntimeException() {
        Mockito.when(userDetailsService.loadUserByUsername("username")).thenReturn(userDetails);

        Token token = jwtTokenProvider.createToken("username", Role.USER);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assertions.assertThrows(RuntimeException.class, () -> jwtTokenProvider.getAuthentication(token.jwt()));
    }

    @Test
    void refreshTokenUsernameReturnsUsername() throws AuthenticationException {
        String refreshToken = jwtTokenProvider.createRefreshToken("username");
        String username = jwtTokenProvider.refreshTokenUsername(refreshToken);
        Assertions.assertEquals("username", username);
    }

    @Test
    void refreshTokenUsernameWithBlacklistedTokenThrowsAuthenticationException() {
        String refreshToken = jwtTokenProvider.createRefreshToken("username");
        Mockito.when(refreshTokenBlacklistService.isBlacklisted(refreshToken)).thenReturn(true);
        Assertions.assertThrows(AuthenticationException.class, () -> jwtTokenProvider.refreshTokenUsername(refreshToken));
    }

    @Test
    void refreshTokenWithExpiredTokenThrowsAuthenticationException() {
        String refreshToken = jwtTokenProvider.createRefreshToken("username");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assertions.assertThrows(ExpiredJwtException.class, () -> jwtTokenProvider.refreshTokenUsername(refreshToken));
    }

    @Test
    void getUsernameReturnsUsername() {
        String token = jwtTokenProvider.createToken("username", Role.USER).jwt();
        Mockito.when(userDetailsService.loadUserByUsername("username")).thenReturn(userDetails);
        jwtTokenProvider.getAuthentication(token);
        String username = jwtTokenProvider.getUsername();
        Assertions.assertEquals("username", username);
    }

    @Test
    void getUsernameWhenAuthenticationIsNullReturnsNull() {
        String username = jwtTokenProvider.getUsername();
        Assertions.assertNull(username);
    }

    @Test
    void getRoleReturnsRole() {
        String token = jwtTokenProvider.createToken("username", Role.USER).jwt();
        Mockito.when(userDetailsService.loadUserByUsername("username")).thenReturn(userDetails);
        User user = new User();
        user.setRole(Role.USER);
        Mockito.when(userRepository.findUserByUsername("username")).thenReturn(Optional.of(user));
        jwtTokenProvider.getAuthentication(token);
        Role role = jwtTokenProvider.getRole();
        Assertions.assertEquals(Role.USER, role);
    }

    @Test
    void getRuleWhenAuthenticationIsNullReturnsNull() {
        Role role = jwtTokenProvider.getRole();
        Assertions.assertNull(role);
    }

    @Test
    void clearAuthenticationWorks() {
        Assertions.assertDoesNotThrow(() -> jwtTokenProvider.clearAuthentication());
    }
}
