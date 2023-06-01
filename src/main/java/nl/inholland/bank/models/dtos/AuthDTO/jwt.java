package nl.inholland.bank.models.dtos.AuthDTO;

public record jwt (String access_token, String refresh_token, int id, long expiresAt) {
}
