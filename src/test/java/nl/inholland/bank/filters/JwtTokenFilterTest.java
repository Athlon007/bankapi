package nl.inholland.bank.filters;

import jakarta.servlet.ServletException;
import nl.inholland.bank.utils.JwtTokenProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc(addFilters = false)
class JwtTokenFilterTest {
    // Mock JwtTokenProvider.
    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain chain;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        chain = new MockFilterChain();
    }


    @Test
    void correctTokenShouldSucceed() {
        request.addHeader("Authorization", "Bearer token");

        Assertions.assertDoesNotThrow(() -> {
            JwtTokenFilter filter = new JwtTokenFilter(jwtTokenProvider);
            filter.doFilterInternal(request, response, chain);
        });
    }

    @Test
    void incorrectTokenShouldGiveUnauthorizedResponse() throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer wrong-token");

        Mockito.when(jwtTokenProvider.getAuthentication("wrong-token")).thenThrow(new RuntimeException("Invalid token"));

        JwtTokenFilter filter = new JwtTokenFilter(jwtTokenProvider);
        filter.doFilterInternal(request, response, chain);

        Assertions.assertEquals(401, response.getStatus());
    }

    @Test
    void noTokenShouldAuthorizeIfAccessingAuth() throws ServletException, IOException {
        request.setRequestURI("/auth/login");
        request.setMethod("POST");

        Mockito.when(jwtTokenProvider.getAuthentication(null)).thenThrow(new RuntimeException("Invalid token"));

        JwtTokenFilter filter = new JwtTokenFilter(jwtTokenProvider);
        filter.doFilterInternal(request, response, chain);

         Assertions.assertEquals(200, response.getStatus());
    }
}
