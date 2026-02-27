package com.example.BloggingApi.Security;

import com.example.BloggingApi.Domain.User;
import com.example.BloggingApi.Repositories.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

// Handles Google login — creates a local User if they're new
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public CustomOAuth2UserService(UserRepository userRepository,
                                  org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = new LinkedHashMap<>(oauth2User.getAttributes());

        String emailResolved = (String) attributes.get("email");
        if (!StringUtils.hasText(emailResolved)) {
            emailResolved = (String) attributes.get("sub"); // fallback to subject
        }
        String name = (String) attributes.get("name");
        if (!StringUtils.hasText(name)) {
            name = emailResolved;
        }
        String usernameResolved = StringUtils.hasText(name) ? name : emailResolved;
        usernameResolved = usernameResolved.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (usernameResolved.length() > 100) {
            usernameResolved = usernameResolved.substring(0, 100);
        }
        final String email = emailResolved;
        final String username = usernameResolved;

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseGet(() -> {
                    String encodedPassword = passwordEncoder.encode(UUID.randomUUID().toString());
                    User newUser = User.createFromOAuth2(username, email, encodedPassword);
                    return userRepository.save(newUser);
                });

        // Stash our DB fields so the success handler can find them
        attributes.put("dbUserId", user.getId());
        attributes.put("dbUsername", user.getUsername());
        attributes.put("dbEmail", user.getEmail());
        attributes.put("dbRole", user.getRole() != null ? user.getRole().toUpperCase() : "READER");

        String role = user.getRole();
        if (role == null || role.isBlank()) {
            role = "READER";
        } else {
            role = role.trim().toUpperCase();
            if ("GENERAL".equals(role)) {
                role = "READER";
            }
        }
        Set<SimpleGrantedAuthority> authorities = Set.of(new SimpleGrantedAuthority("ROLE_" + role));

        String nameAttributeKey = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();

        return new DefaultOAuth2User(authorities, attributes, nameAttributeKey);
    }
}
