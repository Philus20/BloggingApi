# Blogging Platform API

A Spring Boot blogging API with REST and GraphQL support, Spring Data JPA, caching, and transaction management.

## Tech Stack

- **Java 21** · **Spring Boot 3.x**
- **Spring Security** – JWT authentication, Google OAuth2, RBAC
- **Spring Data JPA** – Repositories, pagination, sorting
- **PostgreSQL** – Database
- **Spring Cache** – Caching for posts, users, tags
- **REST API** – JSON over HTTP
- **GraphQL** – `/graphql` endpoint with GraphiQL at `/graphql`
- **OpenAPI/Swagger** – Interactive API docs at `/swagger-ui.html`, OpenAPI JSON at `/v3/api-docs`

---

## Project Structure

```
src/main/java/com/example/BloggingApi/
├── AOP/              # Logging and performance aspects
├── Config/           # CORS, cache, password encoder
├── Controllers/
│   ├── Rest/         # REST controllers (Auth, User, Post, Comment, Tag, Review, SecurityReport)
│   └── Graphql/      # GraphQL controllers
├── Domain/           # JPA entities (User, Post, Comment, Tag, Review)
├── DTOs/             # Requests, Responses, and Validation annotations
├── Exceptions/       # Custom exceptions + GlobalExceptionHandler
├── Filter/           # JWTFilter, CorsOriginFilter
├── Repositories/     # Spring Data JPA repositories
├── Security/         # SecurityConfig, JWTService, OAuth2, RBAC, token stores, event tracking
└── Services/         # Business logic (UserService, PostService, etc.)
```

---

## Repository Structure

All repositories extend `JpaRepository<Entity, Long>`:

| Repository | Entity | Derived Methods | Custom Queries |
|------------|--------|-----------------|----------------|
| `UserRepository` | User | findByUsername, findByUsernameContainingIgnoreCase, findByEmailContainingIgnoreCase | searchByKeyword (JPQL) |
| `PostRepository` | Post | findByTitleContainingIgnoreCase, findByContentContainingIgnoreCase | searchByKeyword, findByAuthorUsername (JPQL), countByAuthorId (native SQL) |
| `CommentRepository` | Comment | findByContentContainingIgnoreCase | findByAuthorUsernameContainingIgnoreCase (JPQL) |
| `TagRepository` | Tag | findByName, findByNameContainingIgnoreCase | — |
| `ReviewRepository` | Review | findByCommentContainingIgnoreCase, findByRating | findByUserUsernameContainingIgnoreCase (JPQL) |

### Query Types

- **Derived queries** – Spring Data infers from method names (e.g. `findByTitleContainingIgnoreCase`)
- **JPQL** – `@Query("SELECT p FROM Post p WHERE ...")`
- **Native SQL** – `@Query(value = "SELECT COUNT(*) FROM posts WHERE user_id = :authorId", nativeQuery = true)`

---

## Transaction Handling

- **@Transactional** on service methods that modify data (create, update, delete)
- **Propagation**: default `REQUIRED` – join existing transaction or create new
- **Rollback**: on any unchecked exception (RuntimeException)
- Create/update/delete operations run in a single transaction for consistency

---

## Caching

### Configuration

- **@EnableCaching** on main application
- **Cache names**: `posts`, `users`, `tags`
- **Default**: in-memory (ConcurrentMapCache)
- **Production**: configure Redis or Caffeine in `application.properties`

### Usage

- **@Cacheable** on `getById()` for Post, User, Tag – caches single-entity lookups
- **@CacheEvict(allEntries = true)** on create/update/delete – clears cache when data changes

### Measuring Performance

1. Call `GET /posts/{id}` twice – second call is faster (cache hit)
2. Call `POST /posts` then `GET /posts/{newId}` – cache was invalidated
3. Use Actuator metrics or logging to compare response times

---

## Running the Application

1. **Prerequisites**: Java 21, PostgreSQL
2. **Database**: Create database `springDb` (or adjust `application-dev.properties`)
3. **Run**: `./mvnw spring-boot:run` (or `mvn spring-boot:run`)

### Profiles

- **dev** (default): GraphiQL enabled, SQL logging, dev DB
- **prod**: Adjust for production (e.g. disable GraphiQL, use prod DB)

---

## Authentication & Authorization

### JWT login

1. `POST /api/v1/auth/login` with `{ "username": "...", "password": "..." }`
2. Response includes a signed JWT (HMAC SHA-256) with claims: `sub` (username), `iat`, `exp`, `role`, `email`, `jti`.
3. Send the token on subsequent requests: `Authorization: Bearer <token>`
4. Expired / tampered / revoked tokens get **401**.
5. Too many failed logins (5 in 5 min) get **429**.

### Google OAuth2

- Start at `/oauth2/authorization/google` (browser redirect flow).
- On success the server creates or looks up the user, issues a JWT, and returns JSON with the token.
- New OAuth2 users default to role **READER**.

### Logout

`POST /api/v1/auth/logout` with the Bearer token. The token's `jti` is added to an in-memory blacklist (`RevokedTokenStore`) and removed from `ActiveTokenStore`.

### Roles (RBAC)

| Role | Can do |
|------|--------|
| **READER** | GET any resource |
| **AUTHOR** | Everything READER can, plus POST/PUT/DELETE posts, comments, tags, reviews |
| **ADMIN** | Everything AUTHOR can, plus DELETE users and access `/api/v1/admin/**` |

Enforced in `SecurityConfig` via URL-based rules and with `@PreAuthorize` on admin endpoints.

### Password storage

Passwords are hashed with **BCrypt** (`PasswordEncoderConfig`). Plain text is never stored.

---

## API Endpoints

### Auth

- `POST /api/v1/auth/login` – get a JWT
- `POST /api/v1/auth/logout` – revoke current token
- `GET /api/v1/admin/security/events` – recent auth events (ADMIN)
- `GET /api/v1/admin/security/sessions` – active sessions (ADMIN)

### REST (all under `/api/v1`)

- `GET/POST /posts`, `GET/PUT/DELETE /posts/{id}`, `GET /posts/search`
- `GET/POST /comments`, `GET/PUT/DELETE /comments/{id}`, `GET /comments/search`
- `GET/POST /users`, `GET/PUT/DELETE /users/{id}`, `GET /users/search`
- `GET/POST /tags`, `GET/PUT/DELETE /tags/{id}`, `GET /tags/search`
- `GET/POST /reviews`, `GET/PUT/DELETE /reviews/{id}`, `GET /reviews/search`

### GraphQL

- Endpoint: `POST /graphql`
- Playground: `GET /graphiql`
- Schema: see `src/main/resources/graphql/`

---

## Testing

```bash
./mvnw test
```

Tests use JUnit 5, Mockito, and `@ExtendWith(MockitoExtension.class)` for service unit tests.

---

## DSA Integration: Pagination & Sorting (Technical Requirement #10)

Sorting and pagination algorithms are **mirrored in repository query performance**: all pagination and sorting execute in the database, not in application memory.

### Implementation

1. **PageableUtils** (`com.example.BloggingApi.Utils.PageableUtils`) centralizes creation of `Pageable` instances:
   - Single source of truth for sort direction and page/size logic
   - Used consistently by all services (Post, User, Comment, Tag, Review)

2. **Database-level execution**: Spring Data JPA translates `Pageable` into SQL:
   - `ORDER BY` for sorting (uses indexes when sort column is indexed)
   - `LIMIT` and `OFFSET` for pagination—no full result set loaded into memory
   - Queries return only the requested page of data

3. **Index alignment**: Sort and filter columns are indexed (see Indexes section below) for efficient query plans.

4. **Efficiency**: O(1) page access via `LIMIT/OFFSET`; sorting leverages B-tree indexes where available.

### How to verify

- Run pagination performance test: `./mvnw test -Dtest=PaginationPerformanceTest`
- Check `PageableUtils` usage across services
- Inspect generated SQL (enable `spring.jpa.show-sql=true`) to confirm `ORDER BY` and `LIMIT`/`OFFSET` in queries

---

## Indexes (Performance)

Entity indexes used by queries and sorting:

| Entity   | Index Columns         | Used For                                           |
|----------|------------------------|----------------------------------------------------|
| **Post** | title, created_at, user_id | Filter by title; sort by createdAt; join by author |
| **Comment** | created_at             | Sort by createdAt; chronological listing           |
| **User** | user_name, email, created_at | Filter/search by username/email; sort by createdAt |
| **Tag**  | name                   | Filter by name; sort by name; uniqueness           |
| **Review** | rating, comment      | Filter by rating; search by comment                |

**Recommended sort fields** (match indexes): `id`, `createdAt`, `title`, `name` (Tag), `rating` (Review).

---

## CORS vs CSRF

| | CORS | CSRF |
|---|------|------|
| **What it does** | Controls which origins can call the API from a browser | Prevents a malicious site from forging requests using a victim's cookies/session |
| **Enforced by** | Browser (reads server response headers) | Server (validates a token on state-changing requests) |
| **This API** | Configured in `CorsConfig`; unknown origins get 403 via `CorsOriginFilter` | Disabled for JWT endpoints (no cookies involved); enabled on `/demo/**` for the form demo |

**When you need CSRF:** any time auth relies on cookies or server sessions (login forms, traditional web apps). The browser sends those credentials automatically, so an attacker can trick it into making requests on behalf of the user.

**When you don't:** stateless JWT APIs like this one. The `Authorization: Bearer` header is never sent automatically by the browser, so there's nothing to forge.

Full details, demo walkthrough, and a Postman/browser test matrix: [docs/CSRF-AND-SESSION-SECURITY.md](docs/CSRF-AND-SESSION-SECURITY.md)

---

## Documentation

- **DSA & Security Optimization**: [docs/DSA-AND-SECURITY-OPTIMIZATION.md](docs/DSA-AND-SECURITY-OPTIMIZATION.md)
- **CSRF & Session Security**: [docs/CSRF-AND-SESSION-SECURITY.md](docs/CSRF-AND-SESSION-SECURITY.md)
- **GraphQL**: [docs/GRAPHQL-GUIDE.md](docs/GRAPHQL-GUIDE.md)
- **Swagger UI**: `http://localhost:8080/swagger-ui.html` – interactive API explorer
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs` – raw spec
