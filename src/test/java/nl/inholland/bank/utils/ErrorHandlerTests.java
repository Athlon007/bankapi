package nl.inholland.bank.utils;

import nl.inholland.bank.configuration.ApiTestConfiguration;
import nl.inholland.bank.models.exceptions.AccountIsNotActiveException;
import nl.inholland.bank.models.exceptions.DailyTransactionLimitException;
import nl.inholland.bank.models.exceptions.UserNotTheOwnerOfAccountException;
import org.hibernate.ObjectNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.naming.AuthenticationException;
import javax.security.auth.login.AccountNotFoundException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@ExtendWith(SpringExtension.class)
@Import(ApiTestConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class ErrorHandlerTests {
    private ErrorHandler errorHandler;

    @BeforeEach
    public void setUp() {
        errorHandler = new ErrorHandler();
    }

    @Test
    void allCustomExceptionsMustBeRepresentedInErrorHandler() {
        // Get all custom exceptions.
        File[] files = new File("src/main/java/nl/inholland/bank/models/exceptions").listFiles();
        if (files == null) {
            Assertions.fail("No custom exceptions found.");
        }

        // Get all methods in ErrorHandler.
        java.lang.reflect.Method[] methods = ErrorHandler.class.getDeclaredMethods();
        if (methods.length == 0) {
            Assertions.fail("No methods found in ErrorHandler.");
        }

        List<String> exceptions = new ArrayList<>();

        // Check if all custom exceptions are represented in ErrorHandler.
        for (File file : files) {
            String exceptionName = file.getName().replace(".java", "");
            boolean found = false;
            for (java.lang.reflect.Method method : methods) {
                if (method.getName().equals("handle" + exceptionName)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                exceptions.add(exceptionName);
            }
        }

        if (exceptions.size() > 0) {
            Assertions.fail("Custom exceptions: " + String.join(", ", exceptions) + " are not represented in ErrorHandler.");
        }
    }

    @Test
    void testException() {
        Assertions.assertDoesNotThrow(() -> errorHandler.handleException(new Exception("Test exception")));
    }

    @Test
    void illegalArgument() {
        Assertions.assertDoesNotThrow(() -> errorHandler.handleIllegalArgumentException(new IllegalArgumentException("Test illegal argument")));
    }

    @Test
    void authentication() {
        Assertions.assertDoesNotThrow(() -> errorHandler.handleAuthenticationException(new AuthenticationException("Test authentication")));
    }

    @Test
    void accessDenied() {
        Assertions.assertDoesNotThrow(() -> errorHandler.handleAccessDeniedException(new AccessDeniedException("Test access denied")));
    }

    @Test
    void objectNotFound() {
        Assertions.assertDoesNotThrow(() -> errorHandler.handleObjectNotFoundException(new ObjectNotFoundException((Object) "Test object not found", "Test")));
    }

    @Test
    void methodArgumentTypeMismatch() {
        Assertions.assertDoesNotThrow(() -> errorHandler.handleMethodArgumentTypeMismatchException(new MethodArgumentTypeMismatchException("Test", String.class, "Test", null, null)));
    }

    @Test
    void requestMethodNotSupported() {
        Assertions.assertDoesNotThrow(() -> errorHandler.handleHttpRequestMethodNotSupportedException(new org.springframework.web.HttpRequestMethodNotSupportedException("Test")));
    }


    @Test
    void methodNotAllowed() {
        Assertions.assertDoesNotThrow(() -> errorHandler.handleMethodNotAllowedException(new org.springframework.web.server.MethodNotAllowedException("Test", null)));
    }

    @Test
    void messageNotReadable() {
        Assertions.assertDoesNotThrow(() -> errorHandler.handleHttpMessageNotReadableException(new HttpMessageNotReadableException("Test")));
    }

    @Test
    void disabled() {
        Assertions.assertDoesNotThrow(() -> errorHandler.handleDisabledException(new DisabledException("Test")));
    }

    @Test
    void mediaTypeNotSupported() {
        Assertions.assertDoesNotThrow(() -> errorHandler.handleHttpMediaTypeNotSupportedException(new HttpMediaTypeNotSupportedException("Test")));
    }

    @Test
    void handleDataIntegrityViolationException() {
        Assertions.assertDoesNotThrow(() -> errorHandler.handleDataIntegrityViolationException(new org.springframework.dao.DataIntegrityViolationException("Test")));
    }

    @Test
    void handleAccountNotFoundException() {
        Assertions.assertDoesNotThrow(() -> errorHandler.handleAccountNotFoundException(new AccountNotFoundException("Test")));
    }

    @Test
    void handleUserNotTheOwnerOfAccountException() {
        Assertions.assertDoesNotThrow(() -> errorHandler.handleUserNotTheOwnerOfAccountException(new UserNotTheOwnerOfAccountException("Test")));
    }

    @Test
    void handleAccountIsNotActiveException() {
        Assertions.assertDoesNotThrow(() -> errorHandler.handleAccountIsNotActiveException(new AccountIsNotActiveException("Test")));
    }

    @Test
    void handleDailyTransactionLimitException() {
        Assertions.assertDoesNotThrow(() -> errorHandler.handleDailyTransactionLimitException(new DailyTransactionLimitException("Test")));
    }

    @Test
    void handleSameAccountTransferException() {
        Assertions.assertDoesNotThrow(() -> errorHandler.handleSameAccountTransferException(new nl.inholland.bank.models.exceptions.SameAccountTransferException("Test")));
    }

    @Test
    void handleInactiveAccountException() {
        Assertions.assertDoesNotThrow(() -> errorHandler.handleInactiveAccountException(new nl.inholland.bank.models.exceptions.InactiveAccountException("Test")));
    }

    @Test
    void handleOperationNotAllowedException() {
        Assertions.assertDoesNotThrow(() -> errorHandler.handleOperationNotAllowedException(new nl.inholland.bank.models.exceptions.OperationNotAllowedException("Test")));
    }

    @Test
    void handleInsufficientFundsException() {
        Assertions.assertDoesNotThrow(() -> errorHandler.handleInsufficientFundsException(new nl.inholland.bank.models.exceptions.InsufficientFundsException("Test")));
    }

    @Test
    void handleTransactionLimitException() {
        Assertions.assertDoesNotThrow(() -> errorHandler.handleTransactionLimitException(new nl.inholland.bank.models.exceptions.TransactionLimitException("Test")));
    }
}
