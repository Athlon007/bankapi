package nl.inholland.bank.utils;

import nl.inholland.bank.models.exceptions.*;
import org.hibernate.ObjectNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.MethodNotAllowedException;

import javax.naming.AuthenticationException;
import javax.security.auth.login.AccountNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestControllerAdvice
public class ErrorHandler {
   public static final String LOG_FILE_TEMPLATE = "log/error-%s.log";

    public void writeToFile(Exception e) {
        // Create directory if it doesn't exist.
        String logDir = System.getProperty("user.dir") + "/log";
        new java.io.File(logDir).mkdir();

        String logFile = String.format(LOG_FILE_TEMPLATE, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss.SSS")));
        try (FileWriter fileWriter = new FileWriter(logFile, true)) {
            PrintWriter printWriter = new PrintWriter(fileWriter);
            e.printStackTrace(printWriter);
            printWriter.close();

            String fullPath = System.getProperty("user.dir") + "/" + logFile;
            System.out.println("Error written to file " + fullPath);
        } catch (Exception ex) {
            System.out.println("Error writing to file " + logFile + ": " + ex.getMessage());
        }
    }

    // IllegalArgumentException.
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArgumentException(IllegalArgumentException e) {
        writeToFile(e);
        return "{\"error_message\": \"" + e.getMessage() + "\"}";
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String handleAuthenticationException(AuthenticationException e) {
        return "{\"error_message\": \"" + e.getMessage() + "\"}";
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String handleAccessDeniedException(AccessDeniedException e) {
        return "{\"error_message\": \"" + e.getMessage() + "\"}";
    }

    @ExceptionHandler(ObjectNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleObjectNotFoundException(ObjectNotFoundException e) {
        writeToFile(e);
        return "{\"error_message\": \"" + e.getMessage() + "\"}";
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public String handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        return "{\"error_message\": \"" + e.getMessage() + "\"}";
    }

    @ExceptionHandler(DisabledException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleDisabledException(DisabledException e) {
        // This one is thrown when a user tries to log in with an account that is disabled.
        return "{\"error_message\": \"" + e.getMessage() + "\"}";
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        // Body is not readable or missing.
        return "{\"error_message\": \"Body does not match the schema, is unreadable, or is missing.\"}";
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        // Argument in the URL is not of the correct type.
        return "{\"error_message\": \"URL argument is invalid.\"}";
    }

    @ExceptionHandler(MethodNotAllowedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public String handleMethodNotAllowedException(MethodNotAllowedException e) {
        // Method is not allowed.
        return "{\"error_message\": \"Method is not allowed.\"}";
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public String handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
        // Media type is not supported.
        String mediaType = e.getContentType() == null ? "null" : e.getContentType().toString();
        return "{\"error_message\": \"Media type of type: " + mediaType + "\"}";
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        return "{\"error_message\": \"" + e.getMessage() + "\"}";
    }

    @ExceptionHandler(AccountNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleAccountNotFoundException(AccountNotFoundException e) {
        return "{\"error_message\": \"" + e.getMessage() + "\"}";
    }

    @ExceptionHandler(UserNotTheOwnerOfAccountException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleUserNotTheOwnerOfAccountException(UserNotTheOwnerOfAccountException e) {
        return "{\"error_message\": \"" + e.getMessage() + "\"}";
    }

    @ExceptionHandler(AccountIsNotActiveException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccountIsNotActiveException(AccountIsNotActiveException e) {
        return "{\"error_message\": \"" + e.getMessage() + "\"}";
    }

    @ExceptionHandler(DailyTransactionLimitException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleDailyTransactionLimitException(DailyTransactionLimitException e) {
        return "{\"error_message\": \"" + e.getMessage() + "\"}";
    }

    @ExceptionHandler(SameAccountTransferException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleSameAccountTransferException(SameAccountTransferException e) {
        return "{\"error_message\": \"" + e.getMessage() + "\"}";
    }

    @ExceptionHandler(InactiveAccountException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleInactiveAccountException(InactiveAccountException e) {
        return "{\"error_message\": \"" + e.getMessage() + "\"}";
    }

    @ExceptionHandler(OperationNotAllowedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleOperationNotAllowedException(OperationNotAllowedException e) {
        return "{\"error_message\": \"" + e.getMessage() + "\"}";
    }

    @ExceptionHandler(InsufficientFundsException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleInsufficientFundsException(InsufficientFundsException e) {
        return "{\"error_message\": \"" + e.getMessage() + "\"}";
    }

    @ExceptionHandler(TransactionLimitException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleTransactionLimitException(TransactionLimitException e) {
        return "{\"error_message\": \"" + e.getMessage() + "\"}";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    // This one catches all other exceptions.
    public String handleException(Exception e) {
        String message = e.getMessage();
        message = message.replaceAll("\"", "'");
        writeToFile(e);
        return "{\"error_message\": \"" + message + "\"}";
    }
}
