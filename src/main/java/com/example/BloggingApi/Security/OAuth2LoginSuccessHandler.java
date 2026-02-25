package com.example.BloggingApi.Security;

import com.example.BloggingApi.Domain.User;
import com.example.BloggingApi.Repositories.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * After successful Google OAuth2 login, issues a JWT and redirects to the frontend with the token.
 */
@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTService jwtService;
    private final UserRepository userRepository;
    private final ActiveTokenStore activeTokenStore;
    private final SecurityEventService securityEventService;

    @Value("${app.oauth2.redirect-uri:http://localhost:8080/oauth2/success}")
    private String redirectUri;

    public OAuth2LoginSuccessHandler(JWTService jwtService, UserRepository userRepository,
                                    ActiveTokenStore activeTokenStore, SecurityEventService securityEventService) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.activeTokenStore = activeTokenStore;
        this.securityEventService = securityEventService;
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
            getRedirectStrategy().sendRedirect(request, response, redirectUri + "?error=no_email");
            return;
        }
        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);
        if (user == null) {
            getRedirectStrategy().sendRedirect(request, response, redirectUri + "?error=user_not_found");
            return;
        }
        String token = jwtService.generateToken(user);
        String jti = jwtService.extractJti(token);
        if (jti != null) {
            activeTokenStore.add(jti, user.getUsername(), System.currentTimeMillis());
        }
        securityEventService.logLoginSuccess(user.getUsername());
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", token)
                .queryParam("username", user.getUsername())
                .build()
                .toUriString();
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
