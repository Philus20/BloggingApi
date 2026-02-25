# OAuth2 and Role-Based Access Control (Epic 4)

This document describes Google OAuth2 login and RBAC (roles, method security, and Postman verification).

---

## 1. Google OAuth2 login (User Story 4.1)

### Overview

- **Spring Security OAuth2 Client** is used for the Google login flow.
- User details (email, name) are **fetched from Google** and **persisted** in the application database.
- **New OAuth2 users** are assigned the **READER** role automatically.
- After a successful login, the app **issues a JWT** and redirects to a success URL so the client can use the token for API calls.

### Configuration

1. **Google Cloud Console**
   - Go to [Google Cloud Console](https://console.cloud.google.com/apis/credentials).
   - Create an **OAuth 2.0 Client ID** (Web application).
   - Add **Authorized redirect URI**: `http://localhost:8080/login/oauth2/code/google`.
   - Copy the **Client ID** and **Client secret**.

2. **Application properties** (e.g. `application-dev.properties` or environment variables):
   - `spring.security.oauth2.client.registration.google.client-id=<your-client-id>`
   - `spring.security.oauth2.client.registration.google.client-secret=<your-client-secret>`
   - Or set `GOOGLE_CLIENT_ID` and `GOOGLE_CLIENT_SECRET` (the app uses these with defaults for local dev).
   - `app.oauth2.redirect-uri=http://localhost:8080/oauth2/success` (where to redirect after login).

### Flow

1. User opens: **`GET /oauth2/authorization/google`** (in the browser).
2. User is redirected to Google to sign in and consent.
3. Google redirects back to **`/login/oauth2/code/google`**.
4. **CustomOAuth2UserService** runs: loads or creates a `User` by email, assigns READER if new.
5. **OAuth2LoginSuccessHandler** runs: finds the user, generates a JWT, redirects to **`app.oauth2.redirect-uri`** with query params **`token`** and **`username`**.
6. The success page (e.g. **`/oauth2/success`**) can display the token so the user (or frontend) can use **`Authorization: Bearer <token>`** for the API.

### Persistence and roles

- **User** is looked up by **email** (from Google). If not found, a new user is created with:
  - **Username**: from Google `name` (sanitized) or email.
  - **Email**: from Google.
  - **Password**: a random value (BCrypt), not used for login.
  - **Role**: **READER**.
- Existing users keep their current role; only new users get READER by default. An admin can later change roles in the database if needed.

---

## 2. Roles and RBAC (User Story 4.2)

### Defined roles

| Role    | Description |
|---------|-------------|
| **ADMIN** | Full access: delete users, manage content, all read/write. |
| **AUTHOR** | Create/update/delete posts, comments, tags, reviews; read all. |
| **READER** | Read-only: GET users, posts, comments, tags, reviews; update own user profile. |

Roles are stored in **User.role** and exposed as Spring Security authorities **ROLE_ADMIN**, **ROLE_AUTHOR**, **ROLE_READER** (see **CustomUserDetailsService** and **CustomOAuth2UserService**). The constants are in **`Security.Roles`**.

### Method-level security (@PreAuthorize)

**@EnableMethodSecurity(prePostEnabled = true)** is enabled in **SecurityConfig**. Controllers use **@PreAuthorize** so that access is enforced at the method level as well as by the security filter chain:

| Endpoint type | Allowed roles |
|---------------|----------------|
| **GET** users, posts, comments, tags, reviews | READER, AUTHOR, ADMIN |
| **PUT** user (profile update) | READER, AUTHOR, ADMIN |
| **POST/PUT/DELETE** posts, comments, tags, reviews | AUTHOR, ADMIN |
| **DELETE** user | ADMIN only |

Examples:

- **UserController**: `@PreAuthorize("hasAnyRole('READER', 'AUTHOR', 'ADMIN')")` on GET/PUT; `@PreAuthorize("hasRole('ADMIN')")` on DELETE.
- **PostController**, **CommentController**, **TagController**, **ReviewController**: `@PreAuthorize("hasAnyRole('READER', 'AUTHOR', 'ADMIN')")` on GET; `@PreAuthorize("hasAnyRole('AUTHOR', 'ADMIN')")` on POST/PUT/DELETE.

---

## 3. Postman tests for role-based access

Prerequisites:

- Obtain JWTs for at least two roles, e.g. **READER** and **ADMIN** (or AUTHOR).
  - **Option A**: Register/login via **POST /api/v1/auth/login** with a user that has the desired role in the DB.
  - **Option B**: Log in with Google (see above), then use the token from the redirect (new users are READER).

### 3.1 READER

1. **Login** as a READER user → copy the JWT.
2. **GET /api/v1/users** with **Authorization: Bearer &lt;READER token&gt;** → expect **200**.
3. **GET /api/v1/posts** with the same token → expect **200**.
4. **POST /api/v1/posts** with body `{ "title": "Test", "content": "Test", "authorId": 1 }` and the same token → expect **403 Forbidden** (READER cannot create posts).
5. **DELETE /api/v1/users/2** with the same token → expect **403 Forbidden** (only ADMIN can delete users).

### 3.2 AUTHOR (or ADMIN)

1. **Login** as AUTHOR (or ADMIN) → copy the JWT.
2. **POST /api/v1/posts** with **Authorization: Bearer &lt;AUTHOR token&gt;** and a valid body → expect **200** (or 201/validation as per your API).
3. **PUT /api/v1/posts/1** with the same token and valid body → expect **200**.
4. **DELETE /api/v1/posts/1** with the same token → expect **200** (or 204).

### 3.3 ADMIN

1. **Login** as ADMIN → copy the JWT.
2. **DELETE /api/v1/users/&lt;id&gt;** with **Authorization: Bearer &lt;ADMIN token&gt;** → expect **200** (or 204).
3. With a READER token, **DELETE /api/v1/users/&lt;id&gt;** again → expect **403**.

### 3.4 No token / invalid token

1. **GET /api/v1/users** without **Authorization** header → expect **401** or **403**.
2. **GET /api/v1/users** with **Authorization: Bearer invalid** → expect **401**.

These steps verify that **path-based rules** and **@PreAuthorize** together enforce READER / AUTHOR / ADMIN as intended and can be checked in Postman.

---

*This document covers Epic 4 (OAuth2 and RBAC) for the Blogging API.*
