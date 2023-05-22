package nl.inholland.bank.filters;

import jakarta.servlet.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LargeRequestFilter implements Filter {
    @Value("${bankapi.application.request.maxsize}")
    private int maxSize;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        int messageSize = servletRequest.getContentLength();
        if (messageSize > maxSize) {
            throw new ServletException("Request size exceeds the limit");
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
