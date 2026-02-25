# DSA and Security Optimization (Epic 5)

This document describes hashing for passwords and tokens, token blacklisting, active session tracking, and security event logging for auditing and brute-force detection.

---

## 1. Hashing for password storage and token verification (User Story 5.1)

### Password storage

- **BCrypt** is used for password hashing via `PasswordEncoder` (see `Config.PasswordEncoderConfig`).
- Passwords are **never** stored in plain text; at registration and when creating users, `PasswordEncoder.encode(password)` is used.
- At login, `AuthenticationManager` verifies the submitted password against the stored hash using `PasswordEncoder.matches()`.

### Token verification

- JWTs are **signed** with **HMAC SHA-256** (see `Security.JWTService`). Verification is done by:
  - Parsing the token and verifying the **signature** with the same secret key (tampered tokens fail).
  - Checking the **exp** claim so expired tokens are rejected.
- Token storage uses a **jti** (JWT ID) claim for blacklisting and session tracking; the token string itself is not stored.

---

## 2. Token blacklisting and active session tracking (User Story 5.1)

### Revoked token store (blacklist)

- **RevokedTokenStore** keeps revoked token IDs in an **in-memory map** (`jti` → expiry time).
- On **logout** (`POST /api/v1/auth/logout` with `Authorization: Bearer <token>`), the token’s **jti** and **exp** are read, then:
  - `revokedTokenStore.revoke(jti, expMs)` adds the token to the blacklist until its natural expiry.
  - The token is removed from the active session store.
- **JWTFilter** checks `revokedTokenStore.isRevoked(jti)` for each request; if the token is revoked, it returns **401** with "Token has been revoked".
- Expired entries are removed by a **scheduled cleanup** (default every 5 minutes, configurable via `app.security.revoked-token-cleanup-ms`).

### Active token store (session tracking)

- **ActiveTokenStore** keeps an **in-memory map** of currently valid sessions: **jti** → (username, issuedAt).
- On **login** (and OAuth2 success), after generating the JWT, `activeTokenStore.add(jti, username, issuedAt)` is called.
- On **logout**, `activeTokenStore.remove(jti)` is called.
- Admins can view active sessions via **GET /api/v1/admin/security/sessions** (see below).

### Logout flow

1. Client sends **POST /api/v1/auth/logout** with header **Authorization: Bearer &lt;token&gt;**.
2. Server extracts **jti** and **exp** from the token, then:
   - Puts **jti** in **RevokedTokenStore** (until **exp**).
   - Removes **jti** from **ActiveTokenStore**.
   - Logs a security event (token revoked).
3. Any later request using that token receives **401** with "Token has been revoked".

---

## 3. Security event logging and reports (User Story 5.2)

### Logging of authentication events

- **SecurityEventService** logs and stores:
  - **LOGIN_SUCCESS** – username, timestamp (and to SLF4J: `SECURITY_AUTH success username=...`).
  - **LOGIN_FAILURE** – username, reason (and `SECURITY_AUTH failure username=... reason=...`).
  - **TOKEN_REJECTED** – when a token is expired, invalid, or revoked (and `SECURITY_TOKEN rejected ...`).
  - **TOKEN_REVOKED** – when a token is revoked on logout.
- These events are written to the application logs and kept in an **in-memory list** (last 1000 events) for reports.

### Security event reports (token usage and access frequency)

- **GET /api/v1/admin/security/events?max=100** (ADMIN only) returns the last **max** security events (type, username, details, timestamp).
- **GET /api/v1/admin/security/sessions** (ADMIN only) returns the current **active sessions** (jti, username, issuedAt) and total count.
- Logs can be analyzed for **token usage** (who logged in, when tokens were rejected/revoked) and **access patterns** (e.g. many requests with the same token).

### Brute-force and unauthorized access detection

- **LoginAttemptTracker** (inside **SecurityEventService**):
  - Counts **failed login attempts per username** in a sliding **5-minute** window.
  - After **5 or more failures** for a username, that username is **blocked** and the next login attempt returns **429 Too Many Requests** with "Account temporarily locked due to too many failed attempts."
  - A **successful login** for that username clears the failure count and unblocks.
- **Log analysis**: Search logs for:
  - `SECURITY_AUTH failure` – high count for a single username suggests brute-force.
  - `SECURITY_TOKEN rejected` – many rejections may indicate stolen/invalid tokens or scanning.
- The in-memory **events** list (from `/admin/security/events`) can be used to build simple reports (e.g. failures per user in the last N events).

---

## 4. Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `app.security.revoked-token-cleanup-ms` | 300000 (5 min) | Interval in ms for cleaning expired entries from the revoked-token store. |

Logging: set `logging.level.com.example.BloggingApi.Security.SecurityEventService=INFO` (or DEBUG) to see SECURITY_AUTH and SECURITY_TOKEN messages in the logs.

---

## 5. Summary

| Feature | Implementation |
|---------|----------------|
| **Password hashing** | BCrypt via `PasswordEncoder`; used at registration and login. |
| **Token verification** | HMAC SHA-256 signature verification + exp check in JWTService/JWTFilter. |
| **Token blacklisting** | RevokedTokenStore (in-memory map jti → expiry); checked in JWTFilter; cleanup job. |
| **Active session tracking** | ActiveTokenStore (in-memory map jti → SessionInfo); updated on login/logout. |
| **Logout** | POST /api/v1/auth/logout with Bearer token; revokes jti and removes from active. |
| **Auth event logging** | SecurityEventService logs success/failure/token rejected/revoked; in-memory event list. |
| **Reports** | GET /api/v1/admin/security/events, GET /api/v1/admin/security/sessions (ADMIN). |
| **Brute-force protection** | 5 failures per username in 5 min → 429 and "Account temporarily locked". |

---

*This document covers Epic 5 (DSA and Security Optimization) for the Blogging API.*
