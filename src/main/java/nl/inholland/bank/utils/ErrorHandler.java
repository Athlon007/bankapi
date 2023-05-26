package nl.inholland.bank.utils;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.naming.AuthenticationException;

@RestControllerAdvice
public class ErrorHandler {
    // IllegalArgumentException.
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String handleIllegalArgumentException(IllegalArgumentException e) {
        return "{\"error_message\": \"" + e.getMessage() + "\"}";
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String handleAuthenticationException(AuthenticationException e) {
        return "{\"error_message\": \"" + e.getMessage() + "\"}";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    // This one catches all other exceptions.
    public String handleException(Exception e) {
        String message = e.getMessage();
        message = message.replaceAll("\"", "'");
        return "{\"error_message\": \"" + message + "\"}";
    }
}
