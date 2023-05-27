package nl.inholland.bank.models.exceptions;

public class UserNotTheOwnerOfAccountException extends Throwable {
    public UserNotTheOwnerOfAccountException(String message) {
        super(message);
    }
}
