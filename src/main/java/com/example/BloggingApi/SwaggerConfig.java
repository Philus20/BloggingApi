package com.example.BloggingApi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
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
                                REST API for a blogging platform. Supports CRUD operations for:
                                - **Posts** – Blog posts with title, content, author
                                - **Comments** – Comments on posts
                                - **Users** – Authors and readers
                                - **Tags** – Categorization of posts
                                - **Reviews** – Ratings and feedback on posts
                                
                                All list endpoints support **pagination** and **sorting**. Use the search endpoints for filtering.""")
                        .contact(new Contact()
                                .name("Blogging API Team")
                                .email("api@blogging.example.com"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local development server")));
    }
}

