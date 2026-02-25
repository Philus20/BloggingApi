# Login Flow & Security Fixes

This document explains how the **login** endpoint works end-to-end and how the security-related errors were resolved.

---

## 1. How Login Works

### 1.1 Request Flow (High Level)

```
Client (Postman / curl / frontend)
    → POST /api/v1/login { "username": "...", "password": "..." }
    → UserController.loginUser()
    → UserService.login()
    → AuthenticationManager.authenticate()
        → CustomUserDetailsService.loadUserByUsername()
        → UserRepository + BCrypt password check
    → SecurityContextHolder + load User entity
    → Controller returns ApiResponse<UserResponse>
```

### 1.2 Step-by-Step Login Flow

| Step | Where | What happens |
|------|--------|--------------|
| 1 | **UserController** | Receives `POST /api/v1/login` with JSON body. `@Valid` triggers validation on `LoginRequest` (username and password must be non-blank). |
| 2 | **UserService.login()** | Builds a `UsernamePasswordAuthenticationToken` with the given username and password and passes it to `AuthenticationManager.authenticate()`. |
| 3 | **AuthenticationManager** (from **SecurityConfig**) | Implemented by Spring’s `ProviderManager` with a single `DaoAuthenticationProvider`. The provider uses `UserDetailsService` to load the user and `PasswordEncoder` to verify the password. |
| 4 | **CustomUserDetailsService.loadUserByUsername()** | Loads the app’s `User` entity by username (case-insensitive via `findByUsernameIgnoreCase`). Builds Spring Security’s `UserDetails` (username, encoded password, authorities) and returns it. |
| 5 | **DaoAuthenticationProvider** | Compares the raw request password with the stored (BCrypt) password. If they match, it returns an `Authentication` (principal = `UserDetails`). If not, it throws `BadCredentialsException`. |
| 6 | **UserService.login()** (after success) | Sets the returned `Authentication` on `SecurityContextHolder`. Then takes the **username from the principal** (exact value from DB) and loads the domain `User` with `UserRepository.findByUsername(username)` so the response uses the same casing as in the DB. |
| 7 | **UserController** | Wraps the `User` in `UserResponse` (id, username, email) and returns `ApiResponse.success("Login successful", userResponse)`. |

### 1.3 Failure Paths

- **Blank username or password** → Validation fails → **400** (handled by `MethodArgumentNotValidException` in `GlobalExceptionHandler`).
- **User not found** → `CustomUserDetailsService` throws `UsernameNotFoundException` → **401** “Invalid username or password”.
- **Wrong password** → `DaoAuthenticationProvider` throws `BadCredentialsException` → **401** “Invalid username or password”.
- **Stored password not BCrypt** (e.g. plain text) → Can trigger `IllegalArgumentException` or `InternalAuthenticationServiceException` → both mapped to **401** in `GlobalExceptionHandler`.

---

## 2. Role of Each Class

### 2.1 UserController (`Controllers.Rest.UserController`)

- **Responsibility:** HTTP layer for user-related operations, including login.
- **Login:** `POST /api/v1/login` with `LoginRequest` body.
- **Behaviour:** Validates request with `@Valid`, calls `UserService.login(request)`, maps the returned `User` to `UserResponse`, and returns `ApiResponse.success(..., userResponse)`.
- **Security:** `/api/v1/**` (including `/api/v1/login`) is **permitAll()** in `SecurityConfig`, so no token is required to call login.

### 2.2 LoginRequest (`DTOs.Requests.LoginRequest`)

- **Responsibility:** DTO for login request body.
- **Fields:** `username`, `password`, both with `@NotBlank`.
- **Usage:** Ensures blank credentials are rejected before any authentication logic and produce a clear **400** response.

### 2.3 UserService (`Services.UserService`)

- **Responsibility:** Business logic for users: create, login, update, delete, get, search.
- **Login:**
  - Calls `authenticationManager.authenticate(UsernamePasswordAuthenticationToken)`.
  - On success: sets `SecurityContextHolder.getContext().setAuthentication(authentication)`, then loads the domain `User` using the principal’s username and returns it.
- **Dependencies:** `UserRepository`, `PasswordEncoder`, `AuthenticationManager` (the bean defined in `SecurityConfig`).
- **Registration:** Uses `PasswordEncoder` to hash passwords with BCrypt before saving, so only BCrypt-hashed passwords are stored.

### 2.4 SecurityConfig (`Security.SecurityConfig`)

- **Responsibility:** Configures HTTP security and defines the `AuthenticationManager` and related beans.
- **SecurityFilterChain:**
  - `/api/v1/**`, `/api/v1/login`, Swagger/OpenAPI paths, etc. → **permitAll()**.
  - All other requests → **authenticated()**.
  - CSRF disabled (stateless API); form login and HTTP Basic disabled; **stateless** session.
- **AuthenticationManager bean:** Built **explicitly** with:
  - `DaoAuthenticationProvider`
  - `UserDetailsService` = `CustomUserDetailsService`
  - `PasswordEncoder` = `BCryptPasswordEncoder`
  - Wrapped in `ProviderManager`.
- **Why explicit bean:** Avoids using `AuthenticationConfiguration.getAuthenticationManager()` as a `@Bean`, which led to circular dependency / StackOverflow and, in another setup, to a **null** `AuthenticationManager`. See “Security errors and fixes” below.

### 2.5 CustomUserDetailsService (`Security.CustomUserDetailsService`)

- **Responsibility:** Implements Spring Security’s `UserDetailsService`: load user by username and return `UserDetails`.
- **loadUserByUsername(String username):**
  - Uses `UserRepository.findByUsernameIgnoreCase(username)` so login is **case-insensitive** (e.g. "king" and "King" both work).
  - Builds `UserDetails` with `user.getUsername()`, `user.getPassword()`, and a single authority `"USER"`.
- **Used by:** `DaoAuthenticationProvider` inside the `AuthenticationManager` to load the user and to compare passwords.

### 2.6 UserRepository (`Repositories.UserRepository`)

- **Responsibility:** Data access for `User` entity.
- **Login-related methods:**
  - `findByUsernameIgnoreCase(String username)` → used by `CustomUserDetailsService` for lookup during authentication.
  - `findByUsername(String username)` → used by `UserService.login()` after success to load the domain `User` by the **exact** username from the principal (avoids case/format mismatches).

### 2.7 GlobalExceptionHandler (`Exceptions.GlobalExceptionHandler`)

- **Responsibility:** Centralized API error handling.
- **Login-related handlers:**
  - `BadCredentialsException` → **401** “Invalid username or password”.
  - `UsernameNotFoundException` → **401** “Invalid username or password”.
  - `InternalAuthenticationServiceException` → **401** “Invalid username or password”.
  - `IllegalArgumentException` with message containing "Encoded password" or "BCrypt" → **401** (e.g. when stored password is not BCrypt).

This keeps login failures returning **401** instead of **500** and avoids leaking internal details.

---

## 3. Security Errors and How They Were Fixed

During implementation, the following issues occurred and were addressed as below.

### 3.1 StackOverflowError on Login

**Symptom:** `authenticate()` appeared in the stack trace in an infinite loop; application crashed with `StackOverflowError`.

**Cause:** In `SecurityConfig`, the `AuthenticationManager` was exposed as a bean by calling `AuthenticationConfiguration.getAuthenticationManager()`. That returns a **delegating** manager that resolves the real manager from the context. When `authenticate()` was called, the delegator tried to obtain the `AuthenticationManager` bean again, which led back to the same delegator → infinite recursion.

**Fix:** **Do not** expose `AuthenticationManager` via `AuthenticationConfiguration.getAuthenticationManager()` as a `@Bean`. Instead, build the manager **explicitly** in `SecurityConfig`:

- Create a `DaoAuthenticationProvider`.
- Set `UserDetailsService` (your `CustomUserDetailsService`) and `PasswordEncoder` (BCrypt) on it.
- Return `new ProviderManager(authProvider)` as the `AuthenticationManager` bean.

There is no delegator and no circular dependency, so no StackOverflow.

---

### 3.2 "AuthenticationManager bean could not be found"

**Symptom:** Application failed to start with:  
`Field authenticationManager in UserService required a bean of type 'org.springframework.security.authentication.AuthenticationManager' that could not be found.`

**Cause:** After removing the problematic `AuthenticationManager` bean (to fix the StackOverflow), no `AuthenticationManager` bean was left. Spring’s default `AuthenticationConfiguration` does not always register one when you also define a custom `UserDetailsService` in the same way, so the context had no bean to inject into `UserService`.

**Fix:** Provide an `AuthenticationManager` bean again, but built **explicitly** as in section 3.1 (ProviderManager + DaoAuthenticationProvider). Then `UserService` can depend on `AuthenticationManager` and the context starts correctly.

---

### 3.3 NullPointerException: "authManager is null"

**Symptom:** Login failed with:  
`Cannot invoke "AuthenticationManager.authenticate(...)" because "authManager" is null`.

**Cause:** As a workaround, `UserService` was changed to use `AuthenticationConfiguration` and call `authenticationConfiguration.getAuthenticationManager()` inside `login()`. In this setup, `getAuthenticationManager()` returned **null** (e.g. because the default configuration did not create and expose a manager when custom security config was present).

**Fix:** Revert to injecting a **real** `AuthenticationManager` bean built in `SecurityConfig` (ProviderManager + DaoAuthenticationProvider). Then `UserService` injects that bean and calls `authenticationManager.authenticate(...)` directly. The manager is never null.

---

### 3.4 Login returned 500 instead of 401 for bad credentials

**Symptom:** Wrong username/password or invalid stored password resulted in **500** and a generic “An unexpected error occurred” message.

**Cause:** Exceptions thrown during authentication (e.g. `BadCredentialsException`, `UsernameNotFoundException`, `InternalAuthenticationServiceException`, or `IllegalArgumentException` from BCrypt) were not handled and fell through to the generic `Exception` handler, which returns 500.

**Fix:** In `GlobalExceptionHandler`, add dedicated `@ExceptionHandler` methods for:

- `BadCredentialsException`
- `UsernameNotFoundException`
- `InternalAuthenticationServiceException`
- `IllegalArgumentException` when the message refers to "Encoded password" or "BCrypt"

All of these return **401** with body `ApiResponse.failure("Invalid username or password")`. Login failures are now consistently **401** and do not expose internal errors.

---

### 3.5 Case-sensitivity and wrong user after login

**Symptom:** User could log in with "king" but the code then failed (e.g. NPE or wrong user) when the DB stored "King".

**Cause:**  
- `CustomUserDetailsService` used exact-match `findByUsername()`, so "king" might not find "King", or the opposite could happen depending on DB collation.  
- After authentication, `UserService` used `req.username()` to load the user again; if the DB had different casing, `findByUsername(req.username())` could return null.

**Fix:**  
- Use **case-insensitive** lookup for login: `UserRepository.findByUsernameIgnoreCase(username)` in `CustomUserDetailsService`.  
- After successful authentication, get the username from the **principal**: `((UserDetails) authentication.getPrincipal()).getUsername()` (this is the exact value from the DB). Then load the domain user with `userRepository.findByUsername(username)`. So the same canonical username is used and casing is consistent.

---

## 4. Summary Diagram

```
                    POST /api/v1/login
                    { username, password }
                              │
                              ▼
                    ┌─────────────────────┐
                    │   UserController    │  @Valid LoginRequest
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │     UserService     │  authenticationManager.authenticate(...)
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │ AuthenticationManager│  ProviderManager(DaoAuthenticationProvider)
                    │   (SecurityConfig)   │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │ CustomUserDetails   │  loadUserByUsername → UserDetails
                    │     Service         │  UserRepository.findByUsernameIgnoreCase
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │ DaoAuthentication   │  passwordEncoder.matches(raw, encoded)
                    │     Provider        │  → BadCredentialsException if no match
                    └──────────┬──────────┘
                               │
           success             │             failure
               ┌───────────────┴───────────────┐
               ▼                               ▼
    SecurityContextHolder              GlobalExceptionHandler
    setAuthentication(...)             → 401 "Invalid username or password"
               │
               ▼
    UserRepository.findByUsername(principal.getUsername())
               │
               ▼
    return User → UserResponse → ApiResponse.success(...)
```

---

## 5. How to Test Login

1. **Register a user** (so the password is stored as BCrypt):
   ```http
   POST /api/v1/register
   Content-Type: application/json

   { "username": "king", "email": "king@example.com", "password": "YourPassword123" }
   ```

2. **Login** with the same credentials:
   ```http
   POST /api/v1/login
   Content-Type: application/json

   { "username": "king", "password": "YourPassword123" }
   ```
   Expected: **200** with `{ "status": true, "message": "Login successful", "data": { "id", "username", "email" } }`.

3. **Wrong password:** Use a different password → **401** “Invalid username or password”.

4. **Blank username or password:** Omit or leave empty → **400** validation error.

---

## 5. CORS Configuration (User Story 1.2)

Global CORS is configured so that external clients (e.g. React, JavaFX) can interact safely with the API. Unauthorized origins are explicitly rejected with **403 Forbidden**.

### 5.1 Configuration

- **Config class:** `Config.CorsConfig` defines allowed origins, methods, and headers (configurable via `application.properties`).
- **Security:** `SecurityConfig` uses `CorsConfigurationSource` so that CORS is applied in the security filter chain.
- **Rejection of unauthorized origins:** `Filter.CorsOriginFilter` runs before the security chain; any request that sends an `Origin` header not in the allowed list receives **403** and a JSON body: `{"error":"CORS not allowed for this origin"}`.

### 5.2 Properties (optional)

Defaults work for local development. Override in `application.properties` or `application-dev.properties`:

| Property | Default | Description |
|----------|---------|-------------|
| `app.cors.allowed-origins` | `http://localhost:3000,http://localhost:5173,http://localhost:8080` | Comma-separated origins (React, Vite, JavaFX, etc.). |
| `app.cors.allowed-methods` | `GET,POST,PUT,PATCH,DELETE,OPTIONS` | Allowed HTTP methods. |
| `app.cors.allowed-headers` | `Authorization,Content-Type,Accept,X-Requested-With,Origin` | Allowed request headers. |
| `app.cors.max-age` | `3600` | Preflight cache duration (seconds). |

### 5.3 Testing with Postman

- Postman does **not** send an `Origin` header by default, so requests are **not** subject to the CORS origin check and should succeed as before (e.g. **200** for login, **401** for bad credentials).
- To test CORS behaviour in Postman, add a header: `Origin: http://localhost:3000` → should succeed (allowed). Then set `Origin: https://evil.example.com` → **403** with body `{"error":"CORS not allowed for this origin"}`.

### 5.4 Testing with a web frontend

1. **Allowed origin:** Run a React/Vite app (or any frontend) on e.g. `http://localhost:3000` or `http://localhost:5173`. Ensure the API base URL points to your backend (e.g. `http://localhost:8080`). Requests should succeed and the response should include CORS headers (e.g. `Access-Control-Allow-Origin: http://localhost:3000`).
2. **Unauthorized origin:** Serve the same app from a non-allowed origin (e.g. different port or `file://`). The browser will either receive **403** from the server (if the request includes that origin) or a CORS error in the console; in both cases the cross-origin request is blocked.
3. **Preflight:** For `POST` with JSON or custom headers, the browser sends an `OPTIONS` preflight. The backend responds with **200** and the appropriate `Access-Control-*` headers when the origin is allowed.

---

## 6. JWT-Based Authentication (Epic 2)

### 6.1 Login endpoint and token generation

- **`POST /api/v1/auth/login`** (and **`POST /api/v1/login`** for backward compatibility) accept `{ "username": "...", "password": "..." }` and return a signed JWT plus identity.
- **Response:** `ApiResponse` with `data` of type `LoginResponse`: `token` (JWT string), `username`, `roles` (e.g. `["READER"]`), `expiresAt` (Unix seconds).
- Use the token for protected endpoints: **`Authorization: Bearer <token>`**.

### 6.2 Token validation and 401 behaviour

- **JWTFilter** runs on every request. If the `Authorization: Bearer <token>` header is present:
  - Token is parsed and **signature verified** (HMAC SHA-256). If the token is **tampered**, the filter responds with **401** and body `{ "status": false, "message": "Invalid token signature", "data": null }`.
  - **Expiration** is checked. If the token is **expired**, the filter responds with **401** and body `{ "status": false, "message": "Token expired", "data": null }`.
- Missing or invalid token on a protected endpoint results in **401** (filter) or **403** (Spring Security when no auth is set).

### 6.3 JWT structure and claims (User Story 2.2)

Tokens are **signed with HMAC SHA-256** (algorithm **HS256**). The payload (claims) include:

| Claim | Description |
|-------|-------------|
| **sub** | Subject – username of the authenticated user. |
| **iat** | Issued At – time at which the JWT was issued (Unix seconds). |
| **exp** | Expiration – time after which the JWT must not be accepted (Unix seconds). |
| **role** | Custom – user role (e.g. READER, AUTHOR, ADMIN). |
| **email** | Custom – user email. |

Configuration (e.g. in `application-dev.properties`):

- `app.jwt.secret` – secret key for signing (must be at least **32 characters** for HS256).
- `app.jwt.expiration-ms` – token validity in milliseconds (default 3600000 = 1 hour).

### 6.4 Decoding and verifying the token in Postman

1. **Obtain a token:** Send `POST /api/v1/auth/login` with body `{ "username": "youruser", "password": "yourpass" }`. Copy the `data.token` value from the response.
2. **Decode payload (no verification):** Use [jwt.io](https://jwt.io). Paste the token; the **Payload** section shows the decoded claims (`sub`, `iat`, `exp`, `role`, `email`). You can verify that `sub` is the username, `iat`/`exp` are Unix timestamps, and `role` matches the user.
3. **Verify signature in Postman:** Send a request to a protected endpoint (e.g. `GET /api/v1/users`) with header **`Authorization: Bearer <your-token>`**. If the token is valid and not expired, you get **200**; if expired or tampered, you get **401** with a JSON message.
4. **Test expired/tampered:** Change one character in the token and send again → **401**. After the token expires (or set a short `app.jwt.expiration-ms` and wait), repeat → **401** with "Token expired".

---

*This document reflects the login implementation, security fixes, CORS configuration, and JWT authentication in the BloggingApi project.*
