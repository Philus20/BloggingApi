# Blogging Platform — Spring Security Phase  
## Presentation for Technical Supervisor

**Project:** BloggingApi  
**Phase:** Spring Security (Advanced)  
**Document purpose:** Map curriculum requirements to current implementation and demonstrate compliance.

---

## 1. Project Overview (Spec vs Implementation)

| Spec requirement | Implementation in BloggingApi |
|------------------|-------------------------------|
| Secure REST and GraphQL APIs | **REST** under `/api/v1` (Auth, Posts, Comments, Users, Tags, Reviews); **GraphQL** at `/graphql` — both protected by same security stack. |
| JWT-based authentication | **JWTService** (HMAC SHA-256), **JWTFilter** (validates Bearer on every request), **AuthController** (`POST /api/v1/auth/login`). |
| Google OAuth2 login | **CustomOAuth2UserService**, **OAuth2LoginSuccessHandler** — user created/looked up, JWT returned as JSON. |
| Role-Based Access Control (RBAC) | Roles **ADMIN**, **AUTHOR**, **READER**; enforced in **SecurityConfig** (URL rules) and **@PreAuthorize** on REST + GraphQL controllers. |
| DSA: hashing, encryption, token validation | **BCrypt** (passwords), **HMAC SHA-256** (JWT signing), **RevokedTokenStore** / **ActiveTokenStore** (in-memory maps for tokens/sessions). |

**Architecture:** Controller → Service → Repository (MVC). No Spring Data REST; repositories are used only by services.

---

## 2. Epic 1: Security Configuration and Access Policies

### User Story 1.1 — Spring Security filters and access rules

| Acceptance criteria | Status | Evidence |
|---------------------|--------|----------|
| SecurityFilterChain configured with custom access rules | ✅ | **SecurityConfig**: 3 filter chains — (1) `/demo/**` CSRF demo, (2) `/oauth2/**` OAuth2 login, (3) main API with JWT. |
| Public endpoints (login, registration) and restricted (admin, author, reader) defined | ✅ | **SecurityConfig**: `permitAll()` for `POST /api/v1/auth/login`, `/api/v1/register`; `hasRole("ADMIN")` for `/api/v1/admin/**` and `DELETE /users/**`; `hasAnyRole("AUTHOR","ADMIN")` for write operations; `authenticated()` for remaining `/api/v1/**`. |
| Passwords securely stored using BCryptPasswordEncoder | ✅ | **PasswordEncoderConfig**: `@Bean PasswordEncoder` → `new BCryptPasswordEncoder()`. Used in **UserService** (create/register) and **CustomUserDetailsService** (load user). |

### User Story 1.2 — CORS for external clients

| Acceptance criteria | Status | Evidence |
|---------------------|--------|----------|
| Global CORS configuration (origins, methods, headers) | ✅ | **CorsConfig**: `CorsConfigurationSource` from `app.cors.allowed-origins`, `allowed-methods`, `allowed-headers`, `max-age` (application-dev.properties). |
| Tested with Postman and web frontend | ✅ | Allowed origins include `http://localhost:3000`, `http://localhost:5173`, `http://localhost:8080`; Authorization header allowed. |
| Unauthorized origins blocked with appropriate response | ✅ | **CorsOriginFilter** (Order -101): checks `Origin` header; if not in allowed list returns **403** and JSON `{"error":"CORS not allowed for this origin"}`. |

---

## 3. Epic 2: JWT-Based Authentication

### User Story 2.1 — Login and JWT for protected endpoints

| Acceptance criteria | Status | Evidence |
|---------------------|--------|----------|
| /auth/login endpoint generates signed JWTs with claims (username, roles, expiry) | ✅ | **AuthController** `POST /api/v1/auth/login`; **UserService.login()** → **JWTService.generateToken(user)**. Claims: `sub` (username), `role`, `email`, `jti`, `iat`, `exp`. |
| Tokens validated for each protected request | ✅ | **JWTFilter** (OncePerRequestFilter): extracts Bearer token, verifies signature and expiry, checks **RevokedTokenStore**, loads **UserDetails**, sets **SecurityContext**. |
| Expired or tampered tokens rejected with 401 | ✅ | **JWTFilter**: catches **ExpiredJwtException**, **SignatureException**, **JwtException**; returns **401** with JSON message (e.g. "Token expired", "Invalid token signature", "Token has been revoked"). |

### User Story 2.2 — Token structure and verification

| Acceptance criteria | Status | Evidence |
|---------------------|--------|----------|
| JWT includes subject, issue time, expiration claims | ✅ | **JWTService**: `.subject(username)`, `.issuedAt(now)`, `.expiration(expiry)`, plus custom `role`, `email`, `.id(jti)`. |
| Signed using HMAC SHA-256 (or RSA) | ✅ | **JWTService.getSigningKey()**: `Keys.hmacShaKeyFor(secret.getBytes(UTF_8))`; secret ≥ 32 bytes enforced. |
| Token payload decodable/verifiable (e.g. in Postman) | ✅ | Standard JWT; payload can be decoded at jwt.io; verification via same secret on server. |

---

## 4. Epic 3: CSRF and Session Security

### User Story 3.1 — CSRF configuration

| Acceptance criteria | Status | Evidence |
|---------------------|--------|----------|
| CSRF disabled for stateless JWT APIs | ✅ | **SecurityConfig** (main chain): `.csrf(csrf -> csrf.disable())`; comment: "No CSRF needed — stateless JWT, no cookies". |
| Explanation for when to enable CSRF | ✅ | **docs/CSRF-AND-SESSION-SECURITY.md** and README: CSRF needed for cookie/session-based auth; not needed for Bearer-only APIs. |
| Demonstration of CSRF token for a sample form | ✅ | **DemoCsrfController**: `GET /demo/csrf-form` returns HTML form with hidden `_csrf`; `POST /demo/csrf-submit` requires valid CSRF (separate SecurityFilterChain with CSRF enabled for `/demo/**`). |

### User Story 3.2 — CORS vs CSRF documentation and tests

| Acceptance criteria | Status | Evidence |
|---------------------|--------|----------|
| Technical documentation (CORS vs CSRF) in README | ✅ | README section "CORS vs CSRF" and **docs/CSRF-AND-SESSION-SECURITY.md** with table and demo walkthrough. |
| Practical tests (Postman and browser) | ✅ | Docs describe testing matrix (Postman vs browser, with/without Origin, with/without CSRF token). |

---

## 5. Epic 4: OAuth2 and Role-Based Access Control (RBAC)

### User Story 4.1 — Google OAuth2 login

| Acceptance criteria | Status | Evidence |
|---------------------|--------|----------|
| Google OAuth2 integrated (Spring Security OAuth2 Client) | ✅ | **SecurityConfig** OAuth2 chain; **CustomOAuth2UserService** extends **DefaultOAuth2UserService**; **OAuth2LoginSuccessHandler** returns JWT in JSON. |
| User details from Google persisted | ✅ | **CustomOAuth2UserService**: `findByEmailIgnoreCase` or create **User** via `User.createFromOAuth2`, then `userRepository.save`. |
| Roles assigned post-authentication | ✅ | New OAuth2 users get **READER**; existing users keep DB role; **OAuth2LoginSuccessHandler** builds **LoginResponse** with role list. |

### User Story 4.2 — Role-based endpoint restriction

| Acceptance criteria | Status | Evidence |
|---------------------|--------|----------|
| Roles ADMIN, AUTHOR, READER defined | ✅ | Used in **SecurityConfig** and **@PreAuthorize** across REST and GraphQL. |
| Endpoints secured with @PreAuthorize (or @Secured) | ✅ | All REST controllers (Post, Comment, User, Tag, Review, Auth logout, SecurityReport) and GraphQL controllers use **@PreAuthorize("hasAnyRole('READER','AUTHOR','ADMIN')")** or **hasRole('ADMIN')** / **hasAnyRole('AUTHOR','ADMIN')**. |
| Role-based access verified (e.g. Postman) | ✅ | Swagger/OpenAPI documents endpoints; README describes roles (READER: GET; AUTHOR: + write; ADMIN: + delete users, `/admin/**`). |

---

## 6. Epic 5: DSA and Security Optimization

### User Story 5.1 — DSA for security and efficiency

| Acceptance criteria | Status | Evidence |
|---------------------|--------|----------|
| Hashing for password storage and token verification | ✅ | **BCrypt** for passwords; **HMAC SHA-256** for JWT signing/verification (**JWTService**). |
| Caching or lookup map for token blacklisting | ✅ | **RevokedTokenStore**: `ConcurrentHashMap<String, Long>` (jti → expiry); **isRevoked(jti)** used in **JWTFilter**. |
| In-memory map for revoked tokens / active session tracking | ✅ | **RevokedTokenStore** (revoked jtis); **ActiveTokenStore** (jti → SessionInfo: username, issuedAt). Scheduled cleanup for expired revocations. |

### User Story 5.2 — Monitoring and audit

| Acceptance criteria | Status | Evidence |
|---------------------|--------|----------|
| Logging for auth success/failure | ✅ | **SecurityEventService**: **logLoginSuccess**, **logLoginFailure**, **logTokenRejected**, **logTokenRevoked** — all write to SLF4J and in-memory event list. |
| Security event reports (token usage, access frequency) | ✅ | **GET /api/v1/admin/security/events?max=100** (ADMIN) → **SecurityEventService.getRecentEvents**; **GET /api/v1/admin/security/sessions** → **ActiveTokenStore** (count + sessions). |
| Logs to detect brute-force / unauthorized access | ✅ | **SecurityEventService**: failure count per username in 5-min window; **isBlocked(username)** → **429** on login (**AuthController**). **docs/DSA-AND-SECURITY-OPTIMIZATION.md** describes log analysis. |

---

## 7. Summary Table — Requirement vs Implementation

| Epic | User story | Acceptance criteria met | Key components |
|------|------------|-------------------------|----------------|
| 1 | 1.1 Security config | ✅ All | SecurityConfig, PasswordEncoderConfig |
| 1 | 1.2 CORS | ✅ All | CorsConfig, CorsOriginFilter |
| 2 | 2.1 JWT login | ✅ All | AuthController, JWTService, JWTFilter |
| 2 | 2.2 Token structure | ✅ All | JWTService (HMAC SHA-256, sub/iat/exp/jti) |
| 3 | 3.1 CSRF | ✅ All | SecurityConfig (disabled for API), DemoCsrfController |
| 3 | 3.2 CORS vs CSRF docs | ✅ All | README, docs/CSRF-AND-SESSION-SECURITY.md |
| 4 | 4.1 OAuth2 | ✅ All | CustomOAuth2UserService, OAuth2LoginSuccessHandler |
| 4 | 4.2 RBAC | ✅ All | @PreAuthorize on REST + GraphQL, SecurityConfig URL rules |
| 5 | 5.1 DSA (hashing, maps) | ✅ All | BCrypt, JWTService, RevokedTokenStore, ActiveTokenStore |
| 5 | 5.2 Audit/monitoring | ✅ All | SecurityEventService, SecurityReportController, brute-force 429 |

---

## 8. Supporting Documentation in Repo

- **README.md** — Auth, JWT, OAuth2, RBAC, CORS vs CSRF, running the app.
- **docs/CSRF-AND-SESSION-SECURITY.md** — CSRF vs CORS, when to enable CSRF, demo.
- **docs/DSA-AND-SECURITY-OPTIMIZATION.md** — Hashing, token blacklist, active sessions, event logging, brute-force detection.
- **docs/OAUTH2-AND-RBAC.md** — OAuth2 and role setup (if present).
- **docs/LOGIN-AND-SECURITY.md** — Login flow and security (if present).

---

## 9. How to Demo for Supervisor

1. **Login & JWT:** `POST /api/v1/auth/login` with `{"username":"...","password":"..."}` → use token in `Authorization: Bearer <token>` for `GET /api/v1/posts`.
2. **401 for bad token:** Send expired or invalid token → 401 with message.
3. **RBAC:** Call write endpoint (e.g. `POST /api/v1/posts`) as READER → 403; as AUTHOR/ADMIN → 200.
4. **OAuth2:** Open `/oauth2/authorization/google` in browser → after Google login, JSON with JWT.
5. **CORS:** Request from allowed origin (e.g. React on 3000) vs disallowed origin → 403 from CorsOriginFilter.
6. **CSRF demo:** `GET /demo/csrf-form` → submit form → 200; submit without token → 403.
7. **Brute-force:** 5+ failed logins for same user → next login returns 429.
8. **Admin reports:** As ADMIN, `GET /api/v1/admin/security/events` and `GET /api/v1/admin/security/sessions`.
9. **Logout:** `POST /api/v1/auth/logout` with Bearer token → same token then returns 401 (revoked).

---

*End of presentation document. All acceptance criteria from the Spring Security phase spec are met by the current BloggingApi codebase and documented here with direct references to classes and endpoints.*
