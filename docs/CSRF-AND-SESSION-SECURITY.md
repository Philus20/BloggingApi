# CSRF and Session Security (Epic 3)

This document covers CSRF configuration for the Blogging API, when to enable CSRF, a demo form-based endpoint, and the difference between CORS and CSRF with practical tests.

---

## 1. CSRF disabled for stateless JWT APIs (User Story 3.1)

### Why CSRF is disabled for the main API

The Blogging API is a **stateless JWT API**:

- Authentication is via **Bearer token** in the `Authorization` header, not via cookies or server-side session.
- **CSRF** (Cross-Site Request Forgery) attacks rely on the browser automatically sending **credentials** (cookies/session) with a request triggered by a malicious site. Because the browser does **not** send custom headers (like `Authorization: Bearer ...`) on cross-origin requests, an attacker cannot forge a request that carries a valid JWT. The user’s token is not sent automatically; the client (e.g. SPA or Postman) must explicitly add it.
- Therefore, **CSRF protection adds no benefit** for endpoints that are protected only by JWT in the `Authorization` header. Disabling CSRF for these paths avoids unnecessary complexity and avoids breaking API clients (e.g. Postman, mobile apps) that do not use cookies.

In **SecurityConfig**, the main **SecurityFilterChain** (for `/api/**`, Swagger, GraphQL, etc.) has:

```java
.csrf(csrf -> csrf.disable())
```

This applies to all stateless JWT-authenticated endpoints.

---

## 2. When to enable CSRF (stateful sessions or form submissions)

Enable CSRF when **any** of the following are true:

- The application uses **server-side sessions** (e.g. session cookie) for authentication. A malicious site could trigger a request from the user’s browser that includes the session cookie and perform actions as the user.
- The application serves **HTML forms** that submit via POST/PUT/DELETE and rely on **cookie-based auth** (or session). Without a CSRF token, a form on an attacker’s page could submit to your site with the user’s cookies.
- You have **browser-based form submissions** (e.g. login form, contact form) where the browser automatically sends cookies.

### How to enable CSRF in Spring Security (when needed)

- **Do not** call `.csrf(csrf -> csrf.disable())` for the relevant filter chain.
- Spring Security enables CSRF by default for that chain. It will:
  - Generate a **CSRF token** and make it available (e.g. in the request or in a cookie/header).
  - Reject state-changing requests (POST, PUT, PATCH, DELETE) that do not include a valid token (in a parameter named `_csrf` or in a header such as `X-CSRF-TOKEN`).
- For **HTML forms**, include a hidden input:  
  `<input type="hidden" name="_csrf" value="<token value>"/>`  
  so the token is submitted with the form.
- For **AJAX/SPA**, read the token from a cookie or a meta tag / endpoint and send it in a header (e.g. `X-CSRF-TOKEN`) or in the request body.

---

## 3. Demonstration: CSRF token mechanism for a form-based endpoint (User Story 3.1)

The project includes a **demo** that shows the CSRF token flow with a real form and endpoint.

### Setup

- A separate **SecurityFilterChain** (higher precedence) applies to **`/demo/**`** with:
  - **CSRF enabled** (default).
  - **Session**: `IF_REQUIRED` (session created when needed for CSRF).
- The rest of the API remains stateless with CSRF disabled.

### Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/demo/csrf-form` | Returns an HTML form that includes a hidden `_csrf` input. |
| POST | `/demo/csrf-submit` | Accepts the form. Spring Security validates the CSRF token; invalid/missing token → **403**. |

### How to test the demo

1. **Browser (recommended)**  
   - Open `http://localhost:8080/demo/csrf-form`.  
   - You see a form with a “Message” field and a hidden `_csrf` field.  
   - Click **Submit**. The form POSTs to `/demo/csrf-submit` with the token.  
   - Expected: **200** and “Form accepted” with your message.

2. **Without CSRF token (403)**  
   - Use Postman or curl:  
     `POST http://localhost:8080/demo/csrf-submit`  
     with body `message=test` (no `_csrf`).  
   - Expected: **403 Forbidden** (CSRF token missing).

3. **With valid token**  
   - First GET `/demo/csrf-form` and copy the `_csrf` value from the HTML (or from a cookie if you use cookie-based token).  
   - POST to `/demo/csrf-submit` with `message=test` and `_csrf=<token>`.  
   - Expected: **200** and success response.

This demonstrates that **state-changing requests** to the form endpoint **require** a valid CSRF token when CSRF is enabled.

---

## 4. CORS vs CSRF: technical overview (User Story 3.2)

| Aspect | CORS | CSRF |
|--------|------|------|
| **Purpose** | Control which **origins** can call your API from the **browser** and which headers/methods are allowed. | Prevent a **malicious site** from triggering **state-changing requests** using the **victim’s credentials** (cookies/session). |
| **Who enforces** | **Browser** (using response headers from the server). | **Server** (validates a token or same-site rules). |
| **Relevant credentials** | Any cross-origin request (e.g. cookies if `credentials: true`, or custom headers). | **Cookie/session** sent automatically by the browser. |
| **Typical mechanism** | Server sends `Access-Control-Allow-Origin`, `Access-Control-Allow-Methods`, etc. Browser blocks or allows the response based on these. | Server issues a **CSRF token**; client must send it back (form field or header). Server rejects requests without a valid token. |
| **Stateless JWT API** | **CORS**: Needed so that a frontend on another origin (e.g. React on port 3000) can call the API and read the response. | **CSRF**: Usually **not** needed, because auth is via `Authorization: Bearer`, which the browser does not send automatically cross-site. |
| **Cookie/session-based app** | **CORS**: Can restrict which origins may send credentials. | **CSRF**: **Needed** to prevent forged requests that carry the user’s session cookie. |

### Interaction in this project

- **CORS**: Configured in **CorsConfig** and applied in **SecurityConfig**. Allows specific origins, methods, and headers so that browser clients (e.g. React, JavaFX WebView) can call the API. Unauthorized origins are blocked (e.g. 403 from **CorsOriginFilter**).
- **CSRF**: **Disabled** for the JWT API (no cookie/session auth). **Enabled** only for the **demo** form under `/demo/**` to demonstrate the token mechanism.

---

## 5. Practical tests: Postman vs browser (User Story 3.2)

### Postman

- **No `Origin` header by default** → CORS checks do not apply; requests succeed as long as auth (e.g. JWT) is valid.
- **JWT API**: Use `Authorization: Bearer <token>`. No CSRF token; CSRF is disabled for `/api/**`.
- **Demo form**:  
  - `POST /demo/csrf-submit` **without** `_csrf` → **403**.  
  - `POST /demo/csrf-submit` **with** `_csrf` (e.g. from GET `/demo/csrf-form`) → **200**.

### Browser (same origin)

- **Same-origin** (e.g. page at `http://localhost:8080/demo/csrf-form` posting to `http://localhost:8080/demo/csrf-submit`): CORS does not restrict; form includes `_csrf` → **200**.

### Browser (cross-origin)

- **Cross-origin** (e.g. React at `http://localhost:3000` calling `http://localhost:8080/api/v1/...`):  
  - **CORS**: Server must allow `http://localhost:3000` (e.g. in `app.cors.allowed-origins`). Otherwise the browser blocks the response.  
  - **CSRF**: Not required for JWT endpoints; the frontend sends `Authorization: Bearer <token>` and does not rely on cookies for API auth.

### Quick test matrix

| Scenario | CORS | CSRF | Expected |
|----------|------|------|----------|
| Postman → `POST /api/v1/auth/login` | N/A (no Origin) | Disabled | 200 with token |
| Postman → `GET /api/v1/users` with Bearer token | N/A | Disabled | 200 |
| Browser same-origin → GET `/demo/csrf-form` then POST `/demo/csrf-submit` with form | N/A | Required | 200 |
| Postman → POST `/demo/csrf-submit` without `_csrf` | N/A | Required | 403 |
| Browser from allowed origin (e.g. localhost:3000) → API with Bearer | Allowed | Disabled | 200 if token valid |
| Browser from disallowed origin → API | Blocked | Disabled | CORS error or 403 from server |

---

*This document covers Epic 3 (CSRF and session security) and CORS vs CSRF for the Blogging API.*
