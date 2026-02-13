# Blogging Platform API

Spring Boot application providing **REST** and **GraphQL** APIs for managing users, blog posts, comments, tags, and reviews. Built with layered architecture, validation, centralized exception handling, AOP for logging and performance monitoring, and OpenAPI documentation.

## Project Overview

This phase delivers a web-based Spring Boot application with:

- **RESTful APIs** following REST conventions with structured responses (`status`, `message`, `data`)
- **GraphQL** schema, queries, and mutations for flexible data access
- **Bean Validation** and custom validators (e.g. unique username/email)
- **Centralized exception handling** via `@RestControllerAdvice`
- **OpenAPI (Swagger)** documentation with Swagger UI
- **AOP** for logging and performance monitoring across service methods
- **Pagination, sorting, and filtering** for list and search endpoints

## Tech Stack

- Java 21, Spring Boot 3.2
- Spring Web (REST), Spring GraphQL
- Spring JDBC, Spring Data Commons (Page, Pageable)
- Bean Validation (Jakarta), Springdoc OpenAPI
- PostgreSQL

## Application Setup and Profiles

- **Dependencies**: Managed in `pom.xml`. Constructor-based dependency injection is used across controllers, services, and repositories.
- **Profiles**:
  - `dev` – default; local DB (e.g. `application-dev.properties` with `DB_USERNAME`, `DB_PASSWORD`)
  - `test` – for tests; `application-test.properties` (e.g. Swagger disabled)
  - `prod` – production; `application-prod.properties` (DB URL, pool, logging)

Activate a profile:

```bash
# dev (default)
./mvnw spring-boot:run

# prod
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

## REST API

Base path: `/api/v1` (users, tags, reviews) and `/api/v1/posts`, `/api/v1/comments`.

- **CRUD** for Users, Posts, Comments, Tags, Reviews
- **Responses**: `ApiResponse<T>` with `status`, `message`, `data`
- **Pagination**: `page`, `size`, `sortBy`, `ascending` query parameters
- **Search/filter**: resource-specific (e.g. posts: keyword/title/author; users: keyword/username/email). Search uses repository methods (e.g. by keyword, by title, by author) so performance benefits from DB indexes on those fields.

Layered flow: **Controller → Service → Repository**.

## Validation, Exception Handling, Documentation

- **Bean Validation**: Request DTOs use `@Valid`, `@NotBlank`, `@Email`, `@Pattern`, etc.
- **Custom validators**: e.g. `@UniqueUsername`, `@UniqueEmail` on `CreateUserRequest`.
- **Centralized exceptions**: `GlobalException` (`@RestControllerAdvice`) handles:
  - `NullException`, `DuplicateEntityException`, `EntityNotFoundException`, `ResourceNotFoundException`, `ValidationException`
  - `MethodArgumentNotValidException` (Bean Validation errors)
  - `IllegalArgumentException` (e.g. invalid search parameters)
- **OpenAPI**: Generated from code and annotations. **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html). **API docs (JSON)**: [http://localhost:8080/api-docs](http://localhost:8080/api-docs).

## GraphQL

- **Schema**: `src/main/resources/graphql/` – `types.graphqls`, `queries.graphqls`, `mutations.graphqls` for User, Post, Comment, Tag, Review (and pagination types).
- **Queries**: e.g. `getUser`, `listUsers`, `searchUsers`, and equivalent for posts, comments, tags, reviews.
- **Mutations**: create/edit/delete for each entity.
- **Coexistence**: REST and GraphQL are both enabled; no path conflicts.
- **Testing**: Use GraphiQL or Altair at the GraphQL endpoint (e.g. `http://localhost:8080/graphql` if so configured).

## AOP (Cross-Cutting Concerns)

Aspects are in `com.example.BloggingApi.Aspects` and apply to **service layer** methods (all services under `com.example.BloggingApi.Services`).

### LoggingAspect

- **@Before**: Logs method entry for every service method.
- **@After**: Logs method exit for every service method.
- **@Around**: Wraps execution with debug-level entry/exit logs.
- **Use**: Centralized logging for CRUD and search/analytics without duplicating log calls in each service.

### PerformanceMonitoringAspect

- **@Around**: Measures execution time (start/end) for every service method.
- **Behaviour**: Logs execution time in ms; logs a **warning** if the operation exceeds 500 ms (“slow operation”).
- **Use**: Monitor response performance for list, search, and CRUD operations; analyse slow queries or heavy logic.

Both aspects use the same pointcut: `execution(* com.example.BloggingApi.Services..*(..))`, so all public methods in `Services` are logged and measured.

## Running the Application

1. Configure DB (e.g. PostgreSQL) and set `DB_USERNAME`, `DB_PASSWORD` (and for prod `DB_URL` if needed).
2. Run: `./mvnw spring-boot:run`
3. REST: e.g. `GET http://localhost:8080/api/v1/posts`
4. Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
5. GraphQL: use your GraphQL endpoint with GraphiQL or Altair

## Tests

- Unit tests for services (Mockito) in `src/test/java/.../Services/`.
- Run: `./mvnw test`
