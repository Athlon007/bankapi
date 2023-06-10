package nl.inholland.bank.models.exceptions;

public class DailyTransactionLimitException extends RuntimeException{
    public DailyTransactionLimitException(String message) {
        super(message);
    }
}
