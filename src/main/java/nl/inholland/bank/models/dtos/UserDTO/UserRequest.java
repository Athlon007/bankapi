package nl.inholland.bank.models.dtos.UserDTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserRequest {
    private String email;
    private String username;
    private String password;
    private String first_name;
    private String last_name;
    private String bsn;
    private String phone_number;
    private String birth_date;
}