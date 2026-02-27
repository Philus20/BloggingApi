package com.example.BloggingApi.Filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// Blocks unknown origins with 403 before Spring Security kicks in
@Component
@Order(-101)
public class CorsOriginFilter extends OncePerRequestFilter {

    private static final String ORIGIN_HEADER = "Origin";

    private final List<String> allowedOrigins;

    public CorsOriginFilter(@Qualifier("corsAllowedOriginsList") List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins != null ? allowedOrigins : List.of();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String origin = request.getHeader(ORIGIN_HEADER);
        if (origin != null && !origin.isBlank()) {
            if (!allowedOrigins.contains(origin)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"CORS not allowed for this origin\"}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
