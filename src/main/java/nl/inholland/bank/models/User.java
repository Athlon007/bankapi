package nl.inholland.bank.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue
    private int id;
    private String firstName;
    private String lastName;
    @Column(unique = true)
    private String email;
    private String bsn;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    @OneToOne(cascade = CascadeType.ALL)
    private Account currentAccount;
    @OneToOne(cascade = CascadeType.ALL)
    private Account savingAccount;
    @Column(unique = true)
    private String username;
    private String password;
    private Role role;

    public User(String firstName,
                String lastName,
                String email,
                String bsn,
                String phoneNumber,
                LocalDate dateOfBirth,
                String username,
                String password,
                Role role) {
        this.setFirstName(firstName);
        this.setLastName(lastName);
        this.setEmail(email);
        this.setBsn(bsn);
        this.setPhoneNumber(phoneNumber);
        this.setDateOfBirth(dateOfBirth);
        this.setUsername(username);
        this.setPassword(password);
        this.setRole(role);
    }

    public void setBsn(String bsn) {
        // Check if the BSN is 8 or 9 digits long
        if (bsn.length() != 8 && bsn.length() != 9) {
            throw new IllegalArgumentException("BSN must be 8 or 9 digits long");
        }

        // Check if the BSN only contains numbers
        if (!bsn.matches("[0-9]+")) {
            throw new IllegalArgumentException("BSN must only contain numbers");
        }

        this.bsn = bsn;
    }

    public void setPhoneNumber(String phoneNumber) {
        // Check if the phone number only contains numbers.
        // Phone number may also start with a '+' sign.
        if (!phoneNumber.matches("\\d+") && !phoneNumber.matches("\\+\\d+")) {
            throw new IllegalArgumentException("Phone number must only contain numbers");
        }

        // Check if phone number is 9 or 10 digits long (excluding the '+' sign).
        String phoneNumberWithoutPlusSign = phoneNumber.replace("+", "");
        if (phoneNumberWithoutPlusSign.length() != 9 && phoneNumberWithoutPlusSign.length() != 10) {
            throw new IllegalArgumentException("Phone number must be 9 or 10 digits long");
        }

        this.phoneNumber = phoneNumber;
    }

    public void setEmail(String email) {
        if (!email.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")) {
            throw new IllegalArgumentException("Email is not valid");
        }

        this.email = email;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        // Check if the date of birth is in the future
        if (dateOfBirth.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date of birth cannot be in the future");
        }

        this.dateOfBirth = dateOfBirth;
    }

    public void setRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }

        this.role = role;
    }

    public void setPassword(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }

        // Password cannot have repeating character only (e.g. 'aaaaaaaa')
        if (password.matches("(.)\\1+")) {
            throw new IllegalArgumentException("Password cannot have repeating characters only");
        }

        // Password must adhere to the following rules:
        // - Must contain at least one digit
        // - Must contain at least one lowercase character
        // - Must contain at least one uppercase character
        // - Must contain at least one special character
        if (!password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+-={}:;'\",./<>?]).{8,}$")) {
            throw new IllegalArgumentException("Password must contain at least one digit, one lowercase character, one uppercase character and one special character");
        }

        this.password = password;
    }
}
