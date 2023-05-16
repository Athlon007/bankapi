package nl.inholland.bank.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String bsn;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private UserType userType;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Account> accounts = new ArrayList<>();
    private String passwordHash;

    public User(String firstName, String lastName, String email, String bsn, String phoneNumber, LocalDate dateOfBirth, UserType userType) {
        this.setFirstName(firstName);
        this.setLastName(lastName);
        this.setEmail(email);
        this.setBsn(bsn);
        this.setPhoneNumber(phoneNumber);
        this.setDateOfBirth(dateOfBirth);
        this.setUserType(userType);
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
        if (!phoneNumber.matches("[0-9]+") && !phoneNumber.matches("\\+[0-9]+")) {
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
}
