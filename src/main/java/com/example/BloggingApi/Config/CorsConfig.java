package com.example.BloggingApi.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// CORS setup — allowed origins, methods, headers all come from application.properties
@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:5173,http://localhost:8080}")
    private String allowedOriginsConfig;

    @Value("${app.cors.allowed-methods:GET,POST,PUT,PATCH,DELETE,OPTIONS}")
    private String allowedMethodsConfig;

    @Value("${app.cors.allowed-headers:Authorization,Content-Type,Accept,X-Requested-With,Origin}")
    private String allowedHeadersConfig;

    @Value("${app.cors.max-age:3600}")
    private long maxAge;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(parseCommaSeparated(allowedOriginsConfig));
        config.setAllowedMethods(parseCommaSeparated(allowedMethodsConfig));
        config.setAllowedHeaders(parseCommaSeparated(allowedHeadersConfig));
        config.setAllowCredentials(true);
        config.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // Shared with CorsOriginFilter
    @Bean("corsAllowedOriginsList")
    public List<String> corsAllowedOriginsList() {
        return parseCommaSeparated(allowedOriginsConfig);
    }

    private static List<String> parseCommaSeparated(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
