package nl.inholland.bank.utils;

import nl.inholland.bank.configuration.ApiTestConfiguration;
import org.hibernate.ObjectNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.naming.AuthenticationException;
import java.io.File;
import java.io.FileWriter;

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
    void testException() {
        errorHandler.handleException(new Exception("Test exception"));
    }

    @Test
    void illegalArgument() {
        errorHandler.handleIllegalArgumentException(new IllegalArgumentException("Test illegal argument"));
    }

    @Test
    void authentication() {
        errorHandler.handleAuthenticationException(new AuthenticationException("Test authentication"));
    }

    @Test
    void accessDenied() {
        errorHandler.handleAccessDeniedException(new AccessDeniedException("Test access denied"));
    }

    @Test
    void objectNotFound() {
        errorHandler.handleObjectNotFoundException(new ObjectNotFoundException((Object) "Test object not found", "Test"));
    }

    @Test
    void methodArgumentTypeMismatch() {
        errorHandler.handleMethodArgumentTypeMismatchException(new MethodArgumentTypeMismatchException("Test", String.class, "Test", null, null));
    }

    @Test
    void requestMethodNotSupported() {
        errorHandler.handleHttpRequestMethodNotSupportedException(new org.springframework.web.HttpRequestMethodNotSupportedException("Test"));
    }


    @Test
    void methodNotAllowed() {
        errorHandler.handleMethodNotAllowedException(new org.springframework.web.server.MethodNotAllowedException("Test", null));
    }

    @Test
    void messageNotReadable() {
        errorHandler.handleHttpMessageNotReadableException(new HttpMessageNotReadableException("Test"));
    }

    @Test
    void disabled() {
        errorHandler.handleDisabledException(new DisabledException("Test"));
    }

    @Test
    void mediaTypeNotSupported() {
        errorHandler.handleHttpMediaTypeNotSupportedException(new HttpMediaTypeNotSupportedException("Test"));
    }
}
