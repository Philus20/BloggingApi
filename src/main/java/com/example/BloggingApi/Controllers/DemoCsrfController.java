package com.example.BloggingApi.Controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Demo endpoints for CSRF token mechanism (Epic 3, User Story 3.1).
 * These paths are protected by a separate SecurityFilterChain with CSRF enabled.
 * See docs/CSRF-AND-SESSION-SECURITY.md.
 */
@RestController
@RequestMapping("/demo")
public class DemoCsrfController {

    /**
     * Returns an HTML form that includes the CSRF token. Submitting the form
     * demonstrates that the server validates the token on POST.
     */
    @GetMapping(value = "/csrf-form", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> csrfForm(HttpServletRequest request) {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken == null) {
            return ResponseEntity.ok()
                    .body("<html><body><p>CSRF token not available (ensure request is under /demo/** with CSRF enabled).</p></body></html>");
        }
        String tokenName = csrfToken.getParameterName();
        String tokenValue = csrfToken.getToken();
        String html = """
                <!DOCTYPE html>
                <html>
                <head><title>CSRF demo form</title></head>
                <body>
                  <h1>CSRF token demo</h1>
                  <p>This form includes a hidden _csrf field. Submit to see CSRF validation in action.</p>
                  <form method="post" action="/demo/csrf-submit">
                    <input type="hidden" name="%s" value="%s"/>
                    <label>Message: <input type="text" name="message" value="Hello CSRF"/></label>
                    <button type="submit">Submit</button>
                  </form>
                </body>
                </html>
                """.formatted(escapeHtml(tokenName), escapeHtml(tokenValue));
        return ResponseEntity.ok().body(html);
    }

    /**
     * Accepts form submission with message and _csrf. Spring Security validates
     * the CSRF token automatically; missing or invalid token returns 403.
     */
    @PostMapping(value = "/csrf-submit", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> csrfSubmit(@RequestParam(defaultValue = "") String message) {
        return ResponseEntity.ok()
                .body("<html><body><h1>Form accepted</h1><p>CSRF token was valid. Message: " + escapeHtml(message) + "</p></body></html>");
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
