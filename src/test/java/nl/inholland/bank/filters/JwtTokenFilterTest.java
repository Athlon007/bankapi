package nl.inholland.bank.filters;

import jakarta.servlet.ServletException;
import nl.inholland.bank.utils.JwtTokenProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class JwtTokenFilterTest {
    // Mock JwtTokenProvider.
    @MockBean
    private JwtTokenProvider mockJwtTokenProvider;

    @Test
    void incorrectTokenShouldThrowException() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAuthType("Bearer " + "incorrecttoken");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        Assertions.assertThrows(ServletException.class, () -> {
            JwtTokenFilter filter = new JwtTokenFilter(mockJwtTokenProvider);
            filter.doFilterInternal(request, response, chain);
        });
    }
}
