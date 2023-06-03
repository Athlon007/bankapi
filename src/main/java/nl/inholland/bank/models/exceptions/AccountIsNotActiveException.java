package nl.inholland.bank.models.exceptions;

public class AccountIsNotActiveException extends Throwable {
    public AccountIsNotActiveException(String message) {
        super(message);
    }
}
