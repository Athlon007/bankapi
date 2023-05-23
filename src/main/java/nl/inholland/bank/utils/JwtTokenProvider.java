package nl.inholland.bank.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {
    @Value("${security.jwt.token.secret-key:secret}")
    private long validityInMilliseconds;
}
