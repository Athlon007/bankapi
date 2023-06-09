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
    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UserForAdminRequest other)) {
            return false;
        }

        return other.getEmail().equals(this.getEmail()) &&
                other.getUsername().equals(this.getUsername()) &&
                other.getPassword().equals(this.getPassword()) &&
                other.getFirstname().equals(this.getFirstname()) &&
                other.getLastname().equals(this.getLastname()) &&
                other.getBsn().equals(this.getBsn()) &&
                other.getPhone_number().equals(this.getPhone_number()) &&
                other.getBirth_date().equals(this.getBirth_date()) &&
                other.getRole().equals(this.getRole());
    }

    @Override
    public int hashCode() {
        return this.getEmail().hashCode() +
                this.getUsername().hashCode() +
                this.getPassword().hashCode() +
                this.getFirstname().hashCode() +
                this.getLastname().hashCode() +
                this.getBsn().hashCode() +
                this.getPhone_number().hashCode() +
                this.getBirth_date().hashCode() +
                this.getRole().hashCode();
    }
}
