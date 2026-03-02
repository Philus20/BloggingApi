package com.example.BloggingApi.Exceptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(GlobalExceptionHandlerTest.TestController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @RestController
    static class TestController {
        @GetMapping("/test/null")
        public String throwNull() {
            throw new NullException("Resource not found");
        }

        @GetMapping("/test/bad-credentials")
        public String throwBadCredentials() {
            throw new BadCredentialsException("Bad credentials");
        }

        @GetMapping("/test/access-denied")
        public String throwAccessDenied() {
            throw new AccessDeniedException("Access denied");
        }

        @PostMapping("/test/valid")
        public String validEndpoint(@RequestBody @Valid TestRequest request) {
            return "ok";
        }

        @GetMapping("/test/illegal-arg")
        public String throwIllegalArg() {
            throw new IllegalArgumentException("Invalid argument");
        }

        @GetMapping("/test/generic")
        public String throwGeneric() {
            throw new RuntimeException("Unexpected error");
        }
    }

    record TestRequest(@NotBlank String name) {}

    @Test
    @WithMockUser(roles = "READER")
    void handleNullException_shouldReturn404() throws Exception {
        mockMvc.perform(get("/test/null"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value("Resource not found"));
    }

    @Test
    @WithMockUser(roles = "READER")
    void handleBadCredentials_shouldReturn401() throws Exception {
        mockMvc.perform(get("/test/bad-credentials"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    @WithMockUser(roles = "READER")
    void handleAccessDenied_shouldReturn403() throws Exception {
        mockMvc.perform(get("/test/access-denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(false));
    }

    @Test
    @WithMockUser(roles = "AUTHOR")
    void handleMethodArgumentNotValid_shouldReturn400() throws Exception {
        mockMvc.perform(post("/test/valid")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false));
    }

    @Test
    @WithMockUser(roles = "READER")
    void handleIllegalArgumentException_shouldReturn400() throws Exception {
        mockMvc.perform(get("/test/illegal-arg"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value("Invalid argument"));
    }

    @Test
    @WithMockUser(roles = "READER")
    void handleGenericException_shouldReturn500() throws Exception {
        mockMvc.perform(get("/test/generic"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred. Please try again later."));
    }
}
