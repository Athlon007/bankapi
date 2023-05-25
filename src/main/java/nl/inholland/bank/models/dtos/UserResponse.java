package nl.inholland.bank.models.dtos;

public record UserResponse(int id, String email, String firstname, String lastname, String bsn, String phone_number, String birth_date, String role) { }
