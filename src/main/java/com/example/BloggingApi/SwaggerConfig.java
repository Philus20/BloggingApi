package com.example.BloggingApi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Blogging Platform API")
                        .version("1.0")
                        .description("""
                                REST API for a blogging platform with JWT authentication and Google OAuth2.
                                
                                **Authentication:** POST to `/api/v1/auth/login` to get a JWT, then click \
                                **Authorize** above and enter `Bearer <token>`.
                                
                                **Roles:** ADMIN, AUTHOR, READER. Write operations need AUTHOR or ADMIN; \
                                user deletion needs ADMIN.
                                
                                Supports CRUD for Posts, Comments, Users, Tags, and Reviews with pagination and sorting.""")
                        .contact(new Contact()
                                .name("Blogging API Team")
                                .email("api@blogging.example.com"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local development server")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer JWT"))
                .components(new Components()
                        .addSecuritySchemes("Bearer JWT", new SecurityScheme()
                                .name("Authorization")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste the token from /api/v1/auth/login")));
    }
}

