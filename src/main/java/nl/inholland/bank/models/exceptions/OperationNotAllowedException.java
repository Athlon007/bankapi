package nl.inholland.bank.models.exceptions;

/**
 * Exception thrown when a user tries to perform an operation that is not allowed
 */
public class OperationNotAllowedException extends RuntimeException{
    public OperationNotAllowedException(String message) {
        super(message);
    }
}
