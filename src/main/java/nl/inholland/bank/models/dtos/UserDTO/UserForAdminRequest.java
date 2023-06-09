package nl.inholland.bank.models.dtos.UserDTO;

public class UserForAdminRequest extends UserRequest {
    private String role;

    public UserForAdminRequest(String email, String username, String password, String firstname, String lastname, String bsn, String phone_number, String birth_date, String role) {
        super(email, username, password, firstname, lastname, bsn, phone_number, birth_date);
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}
