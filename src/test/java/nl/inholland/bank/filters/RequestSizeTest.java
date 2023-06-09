package nl.inholland.bank.filters;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

class RequestSizeTest {
    @Value("${bankapi.application.request.maxsize}")
    private int maxSize;

    @Test
    void requestLargerThanMaxSizeGetsRejectedWithServletException() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContent(new byte[maxSize + 1]);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        Assertions.assertThrows(ServletException.class, () -> {
            LargeRequestFilter filter = new LargeRequestFilter();
            filter.doFilter(request, response, chain);
        });
    }

    @Test
    void requestSmallerThanMaxSizeGetsAccepted() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContent(new byte[maxSize]);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        LargeRequestFilter filter = new LargeRequestFilter();
        filter.doFilter(request, response, chain);
    }
}
