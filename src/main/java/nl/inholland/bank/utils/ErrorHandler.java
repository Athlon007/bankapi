package nl.inholland.bank.utils;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.naming.AuthenticationException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestControllerAdvice
public class ErrorHandler {
   private final String LOG_FILE_TEMPLATE = "log/error-%s.log";

    private void writeToFile(Exception e) {
        // Create directory if it doesn't exist.
        String logDir = System.getProperty("user.dir") + "/log";
        new java.io.File(logDir).mkdir();

        String logFile = String.format(LOG_FILE_TEMPLATE, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss.SSS")));
        try {
            FileWriter fileWriter = new FileWriter(logFile, true);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            e.printStackTrace(printWriter);
            printWriter.close();

            String fullPath = System.getProperty("user.dir") + "/" + logFile;
            System.out.println("Error written to file " + fullPath);
        } catch (IOException ex) {
            System.out.println("Error writing to file " + logFile + ": " + ex.getMessage());
        }
    }

    // IllegalArgumentException.
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String handleIllegalArgumentException(IllegalArgumentException e) {
        writeToFile(e);
        return "{\"error_message\": \"" + e.getMessage() + "\"}";
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String handleAuthenticationException(AuthenticationException e) {
        writeToFile(e);
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
