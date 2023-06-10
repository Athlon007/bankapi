package nl.inholland.bank.models.exceptions;

public class InactiveAccountException extends RuntimeException{
    public InactiveAccountException(String message) {
        super(message);
    }
}
