package nl.inholland.bank.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import nl.inholland.bank.models.Role;
import nl.inholland.bank.models.Token;
import nl.inholland.bank.services.RefreshTokenBlacklistService;
import nl.inholland.bank.services.UserDetailsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.naming.AuthenticationException;
import java.util.Date;

@Component
public class JwtTokenProvider {
    @Value("${bankapi.token.expiration}")
    private long validityInMilliseconds;
    @Value("${bankapi.token.refresh.expiration}")
    private long validityRefreshInMilliseconds;

    private UserDetailsService userDetailsService;
    private JwtKeyProvider jwtKeyProvider;
    private RefreshTokenBlacklistService refreshTokenBlacklistService;

    private Authentication authentication;

    public JwtTokenProvider(UserDetailsService userDetailsService, JwtKeyProvider jwtKeyProvider, RefreshTokenBlacklistService refreshTokenBlacklistService) {
        this.userDetailsService = userDetailsService;
        this.jwtKeyProvider = jwtKeyProvider;
        this.refreshTokenBlacklistService = refreshTokenBlacklistService;
    }

    public Token createToken(String username, Role role) {
        Claims claims = Jwts.claims().setSubject(username);
        Date issuedAt = new Date();
        Date expiresAt = new Date(issuedAt.getTime() + validityInMilliseconds);
        claims.put("auth", role);

        String jwt = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(issuedAt)
                .setExpiration(expiresAt)
                .signWith(jwtKeyProvider.getPrivateKey())
                .compact();
        long expires = expiresAt.getTime();
        return new Token(jwt, expires);
    }

    public String createRefreshToken(String username) {
        Claims claims = Jwts.claims().setSubject(username);
        Date issuedAt = new Date();
        Date expiresAt = new Date(issuedAt.getTime() + validityRefreshInMilliseconds);
        claims.put("auth", "refresh");

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(issuedAt)
                .setExpiration(expiresAt)
                .signWith(jwtKeyProvider.getPrivateKey())
                .compact();
    }

    public Authentication getAuthentication(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(jwtKeyProvider.getPrivateKey())
                    .build()
                    .parseClaimsJws(token);
            String username = claims.getBody().getSubject();
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            this.authentication = new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());
            return authentication;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public String refreshTokenUsername(String refreshToken) throws AuthenticationException {
        try {
            // Check if token is blacklisted.
            if (refreshTokenBlacklistService.isBlacklisted(refreshToken))
                throw new AuthenticationException("Refresh token has already been used.");

            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(jwtKeyProvider.getPrivateKey())
                    .build()
                    .parseClaimsJws(refreshToken);

            // Expire old refresh token.
            refreshTokenBlacklistService.blacklist(refreshToken);
        } catch (Exception e) {
            throw e;
        }
    }

    public String getUsername() {
        // Get username from authentication.
        if (authentication == null) {
            return null;
        }

        return authentication.getName();
    }

    public Role getRole() {
        // Get role from authentication.
        if (authentication == null) {
            return null;
        }

        return (Role) authentication.getAuthorities().stream().findFirst().orElseThrow(
                () -> new RuntimeException("No role found in authentication.")
        );
    }

    public void clearAuthentication() {
        this.authentication = null;
    }
}
