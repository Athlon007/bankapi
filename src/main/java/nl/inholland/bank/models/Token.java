package nl.inholland.bank.models;

public record Token(String jwt, long expiresAt) {
}