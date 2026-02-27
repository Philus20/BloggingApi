package com.example.BloggingApi.Security;

import com.example.BloggingApi.Domain.User;
import com.example.BloggingApi.DTOs.Responses.ApiResponse;
import com.example.BloggingApi.DTOs.Responses.LoginResponse;
import com.example.BloggingApi.Repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTService jwtService;
    private final UserRepository userRepository;
    private final ActiveTokenStore activeTokenStore;
    private final SecurityEventService securityEventService;
    private final ObjectMapper objectMapper;

    public OAuth2LoginSuccessHandler(JWTService jwtService, UserRepository userRepository,
                                    ActiveTokenStore activeTokenStore, SecurityEventService securityEventService,
                                    ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.activeTokenStore = activeTokenStore;
        this.securityEventService = securityEventService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        if (!(authentication.getPrincipal() instanceof OAuth2User oauth2User)) {
            super.onAuthenticationSuccess(request, response, authentication);
            return;
        }

        String email = (String) oauth2User.getAttribute("email");
        if (email == null) {
            email = (String) oauth2User.getAttribute("dbEmail");
        }
        if (email == null) {
            writeErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "OAuth2 provider did not return an email");
            return;
        }

        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);
        if (user == null) {
            writeErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "User not found");
            return;
        }

        String token = jwtService.generateToken(user);
        String jti = jwtService.extractJti(token);
        long expiresAt = jwtService.extractExpiration(token).getTime() / 1000;

        if (jti != null) {
            activeTokenStore.add(jti, user.getUsername(), System.currentTimeMillis());
        }
        securityEventService.logLoginSuccess(user.getUsername());

        String role = user.getRole() != null ? user.getRole().trim().toUpperCase() : "READER";
        LoginResponse loginResponse = new LoginResponse(token, user.getUsername(), List.of(role), expiresAt);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(),
                ApiResponse.success("OAuth2 login successful", loginResponse));
    }

    private void writeErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), ApiResponse.failure(message));
    }
}
