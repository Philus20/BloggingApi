package com.example.BloggingApi.Security;

import com.example.BloggingApi.Filter.JWTFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private static final String API_V1 = "/api/v1";

    private final CustomUserDetailsService userDetailsService;
    private final CorsConfigurationSource corsConfigurationSource;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;

    @Autowired
    private JWTFilter filter;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          CorsConfigurationSource corsConfigurationSource,
                          CustomOAuth2UserService customOAuth2UserService,
                          OAuth2LoginSuccessHandler oauth2LoginSuccessHandler) {
        this.userDetailsService = userDetailsService;
        this.corsConfigurationSource = corsConfigurationSource;
        this.customOAuth2UserService = customOAuth2UserService;
        this.oauth2LoginSuccessHandler = oauth2LoginSuccessHandler;
    }

    /** Demo chain: CSRF enabled for form-based /demo/** (see docs/CSRF-AND-SESSION-SECURITY.md). */
    @Bean
    @Order(1)
    public SecurityFilterChain demoCsrfFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/demo/**")
                .csrf(csrf -> { /* enabled by default */ })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
    }

    /** OAuth2 (Google) login: session-based flow; user details persisted, JWT issued on success redirect. */
    @Bean
    @Order(2)
    public SecurityFilterChain oauth2FilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/oauth2/**", "/login/oauth2/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(ui -> ui.userService(customOAuth2UserService))
                        .successHandler(oauth2LoginSuccessHandler))
                .build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                // CSRF disabled for stateless JWT API: auth is via Bearer token, not cookies/session.
                // Browsers do not send custom headers cross-site, so CSRF adds no benefit here.
                // See docs/CSRF-AND-SESSION-SECURITY.md for when to enable CSRF (stateful sessions, forms).
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Public: login and registration (no authentication required)
                        .requestMatchers(HttpMethod.POST, API_V1 + "/login", API_V1 + "/register", API_V1 + "/auth/login").permitAll()
                        // API docs and Swagger UI (optional: restrict in production)
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        // Admin-only: user deletion and future admin endpoints
                        .requestMatchers(API_V1 + "/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, API_V1 + "/users/**").hasRole("ADMIN")
                        // Author or Admin: create/update/delete content (posts, comments, tags, reviews)
                        .requestMatchers(HttpMethod.POST, API_V1 + "/posts", API_V1 + "/posts/**").hasAnyRole("AUTHOR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, API_V1 + "/posts/**").hasAnyRole("AUTHOR", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, API_V1 + "/posts/**").hasAnyRole("AUTHOR", "ADMIN")
                        .requestMatchers(HttpMethod.POST, API_V1 + "/comments", API_V1 + "/comments/**").hasAnyRole("AUTHOR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, API_V1 + "/comments/**").hasAnyRole("AUTHOR", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, API_V1 + "/comments/**").hasAnyRole("AUTHOR", "ADMIN")
                        .requestMatchers(HttpMethod.POST, API_V1 + "/tags", API_V1 + "/tags/**").hasAnyRole("AUTHOR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, API_V1 + "/tags/**").hasAnyRole("AUTHOR", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, API_V1 + "/tags/**").hasAnyRole("AUTHOR", "ADMIN")
                        .requestMatchers(HttpMethod.POST, API_V1 + "/reviews", API_V1 + "/reviews/**").hasAnyRole("AUTHOR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, API_V1 + "/reviews/**").hasAnyRole("AUTHOR", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, API_V1 + "/reviews/**").hasAnyRole("AUTHOR", "ADMIN")
                        // Reader (and above): all GET and user update require authentication
                        .requestMatchers(API_V1 + "/**").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /** Build AuthenticationManager explicitly to avoid circular dependency from AuthenticationConfiguration. */
    @Bean
    public AuthenticationManager authenticationManager(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(authProvider);
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return userDetailsService;
    }
}