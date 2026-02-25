# Blogging Platform API

A Spring Boot blogging API with REST and GraphQL support, Spring Data JPA, caching, and transaction management.

## Tech Stack

- **Java 21** · **Spring Boot 3.2**
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
├── Config/           # Cache, security, etc.
├── Controllers/      # REST and GraphQL controllers
├── Domain/           # JPA entities (User, Post, Comment, Tag, Review)
├── DTOs/             # Requests and Responses
├── Exceptions/       # Custom exceptions + GlobalExceptionHandler
├── Repositories/     # Spring Data JPA repositories
└── Services/         # Business logic (PostService, CommentService, etc.)
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

## API Endpoints

### REST

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

## Security: CORS and CSRF

The API uses **JWT** in the `Authorization` header for authentication (stateless). **CORS** and **CSRF** are configured as follows so that browser and API clients work correctly.

### CORS vs CSRF (short)

| | CORS | CSRF |
|---|------|------|
| **Purpose** | Controls which **origins** can call the API from the browser and with which methods/headers. | Prevents a malicious site from forging **state-changing requests** using the user’s **cookies/session**. |
| **Enforced by** | Browser (using server response headers). | Server (validates a CSRF token). |
| **This API** | Configured in `Config.CorsConfig`; allowed origins/methods/headers are configurable. Unauthorized origins get **403**. | **Disabled** for the JWT API (auth is Bearer token, not cookies). **Enabled** only for the demo form under `/demo/**` to demonstrate the token mechanism. |

### Why CSRF is disabled for the JWT API

- Authentication is via **Bearer token**, not cookies. Browsers do not send custom headers cross-site, so an attacker cannot forge a request that carries the user’s JWT. CSRF protection adds no benefit for these endpoints.

### When to enable CSRF

- Enable CSRF when using **server-side sessions** or **cookie-based auth**, or when accepting **form submissions** from the browser. See [docs/CSRF-AND-SESSION-SECURITY.md](docs/CSRF-AND-SESSION-SECURITY.md) for details and for a **demo form** at `/demo/csrf-form` that uses a CSRF token.

### Practical tests

- **Postman**: No `Origin` header by default, so CORS does not apply; use `Authorization: Bearer <token>` for protected API calls. For the demo, `POST /demo/csrf-submit` without `_csrf` returns **403**; with a valid token (from GET `/demo/csrf-form`) it returns **200**.
- **Browser**: Open `http://localhost:8080/demo/csrf-form`, submit the form → **200**. Cross-origin calls to the API require the origin to be in the CORS allowed list.

Full technical explanation, when to enable CSRF, and the CORS vs CSRF interaction are in **[docs/CSRF-AND-SESSION-SECURITY.md](docs/CSRF-AND-SESSION-SECURITY.md)**.

---

## Documentation

- **GraphQL**: [docs/GRAPHQL-GUIDE.md](docs/GRAPHQL-GUIDE.md)
- **Login & security**: [docs/LOGIN-AND-SECURITY.md](docs/LOGIN-AND-SECURITY.md)
- **CSRF and session security, CORS vs CSRF**: [docs/CSRF-AND-SESSION-SECURITY.md](docs/CSRF-AND-SESSION-SECURITY.md)
- **OAuth2 (Google) and RBAC**: [docs/OAUTH2-AND-RBAC.md](docs/OAUTH2-AND-RBAC.md)
- **DSA and security optimization**: [docs/DSA-AND-SECURITY-OPTIMIZATION.md](docs/DSA-AND-SECURITY-OPTIMIZATION.md)
- **Swagger UI**: `http://localhost:8080/swagger-ui.html` – interactive API explorer
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs` – raw spec
