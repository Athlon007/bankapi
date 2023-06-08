package nl.inholland.bank.models.exceptions;

public class AccountIsNotActiveException extends RuntimeException {
    public AccountIsNotActiveException(String message) {
        super(message);
    }
}
