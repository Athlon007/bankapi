package nl.inholland.bank.models.exceptions;

public class SameAccountTransferException extends RuntimeException{
    public SameAccountTransferException(String message) {
        super(message);
    }
}
