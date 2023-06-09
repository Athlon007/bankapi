package nl.inholland.bank.models.exceptions;

public class UserNotTheOwnerOfAccountException extends RuntimeException {
    public UserNotTheOwnerOfAccountException(String message) {
        super(message);
    }
}
