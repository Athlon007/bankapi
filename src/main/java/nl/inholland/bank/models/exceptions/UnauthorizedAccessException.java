package nl.inholland.bank.models.exceptions;

@Deprecated(since = "User javax.naming.AuthenticationException instead, please.")
public class UnauthorizedAccessException extends Throwable {
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
