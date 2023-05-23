package nl.inholland.bank.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import nl.inholland.bank.models.Role;
import nl.inholland.bank.services.UserDetailsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {
    @Value("${bankapi.token.expiration}")
    private long validityInMilliseconds;

    private UserDetailsService userDetailsService;
    private JwtKeyProvider jwtKeyProvider;

    private Authentication authentication;


    public JwtTokenProvider(UserDetailsService userDetailsService, JwtKeyProvider jwtKeyProvider) {
        this.userDetailsService = userDetailsService;
        this.jwtKeyProvider = jwtKeyProvider;
    }

    public String createToken(String username, Role role) {
        Claims claims = Jwts.claims().setSubject(username);
        Date issuedAt = new Date();
        Date expiresAt = new Date(issuedAt.getTime() + validityInMilliseconds);
        claims.put("auth", role);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(issuedAt)
                .setExpiration(expiresAt)
                .signWith(jwtKeyProvider.getPrivateKey())
                .compact();
    }

    public String createRefreshToken(String username) {
        Claims claims = Jwts.claims().setSubject(username);
        Date issuedAt = new Date();
        Date expiresAt = new Date(issuedAt.getTime() + validityInMilliseconds);
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

    public String refreshTokenUsername(String refreshToken) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(jwtKeyProvider.getPrivateKey())
                    .build()
                    .parseClaimsJws(refreshToken);
            // Return only if token is still valid.
            if (claims.getBody().getExpiration().after(new Date())) {
                return claims.getBody().getSubject();
            } else {
                throw new RuntimeException("Refresh token is expired.");
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Role getRole() {
        // Get role from authentication.
        return (Role) authentication.getAuthorities().stream().findFirst().get();
    }
}
