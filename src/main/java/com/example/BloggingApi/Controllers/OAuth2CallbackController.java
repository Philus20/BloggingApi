package com.example.BloggingApi.Controllers;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Serves the OAuth2 success redirect URL. Displays the JWT and username from query params
 * so the user (or frontend) can use the token for API calls.
 */
@RestController
public class OAuth2CallbackController {

    @GetMapping(value = "/oauth2/success", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> success(
            @RequestParam(required = false) String token,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String error) {
        if (error != null) {
            return ResponseEntity.ok().body(
                    "<!DOCTYPE html><html><head><title>OAuth2</title></head><body><h1>Login issue</h1><p>Error: "
                            + escape(error) + "</p></body></html>");
        }
        if (token == null || token.isBlank()) {
            return ResponseEntity.ok().body(
                    "<!DOCTYPE html><html><head><title>OAuth2</title></head><body><h1>No token</h1><p>No token received.</p></body></html>");
        }
        String html = """
                <!DOCTYPE html>
                <html><head><title>Login successful</title></head><body>
                  <h1>Logged in with Google</h1>
                  <p>Username: %s</p>
                  <p>Use the token below in <code>Authorization: Bearer &lt;token&gt;</code> for API requests.</p>
                  <pre id="token" style="word-break:break-all;">%s</pre>
                  <button onclick="navigator.clipboard.writeText(document.getElementById('token').textContent)">Copy token</button>
                </body></html>
                """.formatted(escape(username != null ? username : ""), escape(token));
        return ResponseEntity.ok().body(html);
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
