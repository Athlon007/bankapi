package nl.inholland.bank.models;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nl.inholland.bank.models.exceptions.OperationNotAllowedException;
import org.springframework.lang.NonNullFields;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor(force = true)
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue
    private int id;
    @NonNull
    private String firstName;
    private String lastName;
    @Column(unique = true)
    @NonNull
    private String email;
    private String bsn;
    private String phoneNumber;
    @NonNull
    private LocalDate dateOfBirth;
    @OneToOne(cascade = CascadeType.ALL)
    @Nullable
    private Account currentAccount;
    @OneToOne(cascade = CascadeType.ALL)
    @Nullable
    private Account savingAccount;
    @Column(unique = true)
    @NonNull
    private String username;
    @NonNull
    private String password;
    @NonNull
    private Role role;
    @OneToOne(cascade = CascadeType.ALL)
    private Limits limits;
    @NonNull
    private boolean active = true; // User is active by default

    public User(
            String firstName,
            String lastName,
            String email,
            String bsn,
            String phoneNumber,
            LocalDate dateOfBirth,
            String username,
            String password,
            Role role
    ) {
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

    public void setFirstName(String name) {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("First name cannot be empty");
        }

        this.firstName = name;
    }

    public void setLastName(String name) {
        if (name == null || name.length() == 0) {
            // Why we change null to empty string?
            // Because there are people who don't have a last name (yes, really).
            // https://en.wikipedia.org/wiki/List_of_legally_mononymous_people
            name = "";
        }

        this.lastName = name;
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

        // Valid BSN must be divisible by 11, according ot 'elfproef'
        // https://financieel.infonu.nl/diversen/180745-elfproef-voor-rekeningnummer-of-burgerservicenummer-bsn.html
        int length = bsn.length();
        int sum = 0;
        for (int i = 0; i < length; i++) {
            int digit = Character.getNumericValue(bsn.charAt(i));
            int multiplyBy = length - i;
            if (multiplyBy == 1) {
                multiplyBy *= -1;
            }
            sum += digit * multiplyBy;
        }

        if (sum % 11 != 0) {
            throw new IllegalArgumentException("BSN is not valid");
        }

        this.bsn = bsn;
    }

    public void setPhoneNumber(String phoneNumber) {
        // Check if the phone number only contains numbers.
        // Phone number may also start with a '+' sign.
        if (phoneNumber == null || (!phoneNumber.matches("\\d+") && !phoneNumber.matches("\\+\\d+"))) {
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
        if (!password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+={}:;'\",.<>?]).{8,}$")) {
            throw new IllegalArgumentException("Password must contain at least one digit, one lowercase character, one uppercase character and one special character");
        }

        this.password = password;
    }

    public void setUsername(String username) {
        if (username == null || username.length() == 0) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        this.username = username;
    }

    public void setCurrentAccount(Account currentAccount) {
        if (this.currentAccount != null) {
            if (currentAccount == null) {
                throw new OperationNotAllowedException("Cannot unbind current account");
            }
            throw new OperationNotAllowedException("User already has a current account");
        }

        if (currentAccount.getType() != AccountType.CURRENT) {
            throw new IllegalArgumentException("Account type must be CURRENT");
        }

        this.currentAccount = currentAccount;
    }

    public void setSavingAccount(Account savingAccount) {
        // User wants to remove saving account.
        // Check if the saving account has a balance of 0.
        if (savingAccount == null && this.savingAccount.getBalance() > 0) {
            throw new OperationNotAllowedException("Cannot remove saving account with balance");
        }
        else if (savingAccount != null) {
            if (savingAccount.getType() != AccountType.SAVING) {
                throw new IllegalArgumentException("Account type must be SAVING");
            }

            // We cannot set saving account, if user does not have a current account.
            if (this.currentAccount == null) {
                throw new OperationNotAllowedException("Cannot set saving account without current account");
            }
        }

        this.savingAccount = savingAccount;
    }

    public void setLimits(Limits limits) {
        if (limits == null) {
            throw new IllegalArgumentException("Limits cannot be null");
        }

        limits.setUser(this);
        this.limits = limits;
    }

    public double getTotalBalance() {
        double sum = 0;
        if (currentAccount != null) {
            sum += currentAccount.getBalance();
        }
        if (savingAccount != null) {
            sum += savingAccount.getBalance();
        }

        return sum;
    }
}
