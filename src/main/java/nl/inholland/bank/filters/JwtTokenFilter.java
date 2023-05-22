package nl.inholland.bank.filters;

import nl.inholland.bank.utils.JwtTokenProvider;
import org.springframework.stereotype.Component;

import jakarta.servlet.*;

import java.io.IOException;

@Component
public class JwtTokenFilter implements Filter {
    private final JwtTokenProvider jwtTokenProvider;

    public JwtTokenFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
