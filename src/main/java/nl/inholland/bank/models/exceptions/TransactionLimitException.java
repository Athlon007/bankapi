package nl.inholland.bank.models.exceptions;

public class TransactionLimitException extends RuntimeException{
    public TransactionLimitException(String message) {
        super(message);
    }
}
