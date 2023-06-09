package nl.inholland.bank.filters;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nl.inholland.bank.utils.JwtTokenProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import jakarta.servlet.*;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;

    public JwtTokenFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        String token = getToken(request);

        // Before each check, clear the old authentication.
        jwtTokenProvider.clearAuthentication();


        try {
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            // Skip for login endpoint and POST /users, so that we can create a user.
            // We are doing the skip here, because admin can user POST /users to create a user, in order to create Employees and Admins.
            // If we do it before the try-catch block, then admin credentials won't be checked.
            if (
                (request.getRequestURI().equals("/auth/login"))
                        || (request.getRequestURI().equals("/auth/refresh")
                        || (request.getRequestURI().equals("/users") && request.getMethod().equals("POST")))
            ) {
                filterChain.doFilter(request, response);
                return;
            }

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\n\"error_message\": \"Bearer token is missing, invalid or expired.\"\n}");
            response.getWriter().flush();
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        return header.substring(7);
    }
}
