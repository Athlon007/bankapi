package nl.inholland.bank.models.dtos.AuthDTO;

public record jwt (String access_token, String refresh_token, String username, Integer expiresAt) {
}
