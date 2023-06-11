package nl.inholland.bank.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import nl.inholland.bank.models.exceptions.OperationNotAllowedException;

import java.time.LocalDate;
import java.util.Objects;

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
    @NonNull
    private String lastName;
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
    @ToString.Exclude
    @JsonIgnore // Better safe than sorry
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
            throw new IllegalArgumentException("Last name cannot be empty");
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

        // User must be 18 years or older
        if (dateOfBirth.plusYears(18).isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("User must be 18 years or older");
        }

        this.dateOfBirth = dateOfBirth;
    }

    public void setRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }

        this.role = role;
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

        if (currentAccount.getType() == null || currentAccount.getType() != AccountType.CURRENT) {
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

        if (savingAccount != null && (savingAccount.getType() == null || savingAccount.getType() != AccountType.SAVING)) {
            throw new IllegalArgumentException("Account type must be SAVING");
        }

        // We cannot set saving account, if user does not have a current account.
        if (this.currentAccount == null) {
            throw new OperationNotAllowedException("Cannot set saving account without current account");
        }

        if (!this.currentAccount.isActive()) {
            throw new OperationNotAllowedException("Cannot set saving account when current account is inactive");
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

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof User user)) {
            return false;
        }

        return user.getId() == this.getId();
    }

  @Override
  public int hashCode() {
    return Objects.hash(id);
    }
}
