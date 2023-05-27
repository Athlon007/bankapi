package nl.inholland.bank.models.exceptions;

public class UnauthorizedAccessException extends Throwable {
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
