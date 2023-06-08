package nl.inholland.bank.models.dtos;

/**
 * Used to transfer Token with JWT between services. It is never saved into the database.
 * @param jwt The JWT
 * @param expiresAt The expiration date of the JWT
 */
public record Token(String jwt, long expiresAt) {
}
