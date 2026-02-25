package com.example.BloggingApi.Filter;

import com.example.BloggingApi.DTOs.Responses.ApiResponse;
import com.example.BloggingApi.Security.CustomUserDetailsService;
import com.example.BloggingApi.Security.JWTService;
import com.example.BloggingApi.Security.RevokedTokenStore;
import com.example.BloggingApi.Security.SecurityEventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Validates JWT on each protected request. Expired or tampered tokens receive 401 Unauthorized.
 */
@Component
public class JWTFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JWTFilter.class);

    @Autowired
    private JWTService jwtService;

    @Autowired
    private RevokedTokenStore revokedTokenStore;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private SecurityEventService securityEventService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                username = jwtService.extractUsername(token);
            } catch (ExpiredJwtException e) {
                securityEventService.logTokenRejected(null, "Token expired");
                log.warn("JWT rejected: token expired");
                sendUnauthorized(response, "Token expired");
                return;
            } catch (SignatureException e) {
                securityEventService.logTokenRejected(null, "Invalid signature");
                log.warn("JWT rejected: invalid signature");
                sendUnauthorized(response, "Invalid token signature");
                return;
            } catch (JwtException e) {
                securityEventService.logTokenRejected(null, "Invalid token");
                log.warn("JWT rejected: {}", e.getMessage());
                sendUnauthorized(response, "Invalid token");
                return;
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                String jti = jwtService.extractJti(token);
                if (jti != null && revokedTokenStore.isRevoked(jti)) {
                    securityEventService.logTokenRejected(username, "Token revoked");
                    log.warn("JWT rejected: token revoked for user={}", username);
                    sendUnauthorized(response, "Token has been revoked");
                    return;
                }
                UserDetails userDetails = context.getBean(CustomUserDetailsService.class).loadUserByUsername(username);
                if (jwtService.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                securityEventService.logTokenRejected(username, e.getMessage());
                sendUnauthorized(response, "Invalid or expired token");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(new ObjectMapper().writeValueAsString(ApiResponse.failure(message)));
    }
}
