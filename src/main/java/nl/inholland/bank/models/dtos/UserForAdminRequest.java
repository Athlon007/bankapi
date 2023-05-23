package nl.inholland.bank.models.dtos;

public record UserForAdminRequest(String email, String username, String password, String first_name, String last_name, String bsn, String phone_number, String birth_date, String[] roles) {
}
