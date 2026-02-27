## Phase 2: Spring Data, Transactions, Caching – Mentor Presentation Guide

This short guide helps you explain to your mentor **how you fulfilled the Week 6/Phase 2 requirements** (Spring Data, queries, pagination, transactions, caching).

---

## Epic 1 – Spring Data Integration

- **Spring Data JPA configured**
  - Dependency is included and the app runs with `spring.jpa` properties in `application-dev.properties`.
  - The application uses PostgreSQL as configured in `application-dev.properties`.

- **Entities annotated with JPA**
  - `User`, `Post`, `Comment`, `Tag`, `Review` in the `Domain` package are annotated with:
    - `@Entity`, `@Id`, `@GeneratedValue`
    - Relationships: `@ManyToOne`, `@OneToMany`, `@ManyToMany`, `@JoinColumn`, etc.

- **Repositories extend `JpaRepository`**
  - `UserRepository`, `PostRepository`, `CommentRepository`, `TagRepository`, `ReviewRepository` in `Repositories` all extend `JpaRepository<..., Long>`.
  - This gives you CRUD, pagination, sorting, and query derivation for free.

**How to present:**  
“I integrated Spring Data JPA by mapping all my domain classes as `@Entity` and creating repository interfaces that extend `JpaRepository`. The app connects to PostgreSQL via `application-dev.properties`, so persistence is handled in a consistent way.”

---

## Epic 2 – Repository & Query Development

- **Repository interfaces for all aggregates**
  - `UserRepository`, `PostRepository`, `CommentRepository`, `TagRepository`, `ReviewRepository` exist and handle CRUD via Spring Data.

- **Derived query methods**
  - Examples:
    - `UserRepository`: `findByUsername`, `findByUsernameContainingIgnoreCase`, `findByEmailContainingIgnoreCase`
    - `PostRepository`: `findByTitleContainingIgnoreCase`, `findByContentContainingIgnoreCase`
    - `CommentRepository`: `findByContentContainingIgnoreCase`
    - `TagRepository`: `findByName`, `findByNameContainingIgnoreCase`
    - `ReviewRepository`: `findByCommentContainingIgnoreCase`, `findByRating`

- **Custom JPQL and native queries**
  - JPQL:
    - `PostRepository.searchByKeyword(...)` – search in title or content.
    - `PostRepository.findByAuthorUsernameContainingIgnoreCase(...)`
    - `UserRepository.searchByKeyword(...)`
    - `CommentRepository.findByAuthorUsernameContainingIgnoreCase(...)`
    - `ReviewRepository.findByUserUsernameContainingIgnoreCase(...)`
  - Native:
    - `PostRepository.countByAuthorId(...)` uses a native SQL `@Query` to count posts by author.

**How to present:**  
“For CRUD and simple filters I rely on derived query methods in my repositories. For more complex conditions (search by keyword, author username, counts) I use `@Query` with JPQL and one native query for `countByAuthorId`.”

---

## Epic 2 – Pagination & Sorting (User Story 2.2)

- **Centralized `Pageable` creation**
  - `Utils/PageableUtils` builds `Pageable` with page, size, sort field and direction.

- **Services expose paginated APIs**
  - `PostService`, `CommentService`, `UserService`, `TagService`, `ReviewService` methods accept `(page, size, sortBy, ascending)` and delegate to repositories using `PageableUtils.create(...)`.
  - Controllers use these service methods so REST endpoints for posts, comments, etc. return `Page<...>` (paginated responses).

- **Performance testing**
  - `PaginationPerformanceTest` verifies that:
    - `PostService.getAll(...)` and `search(...)` pass `Pageable` down to the repository.
    - Pagination is done at **database level** (via Spring Data translation to `LIMIT/OFFSET`), not in memory.

**How to present:**  
“I implemented pagination and sorting using Spring Data’s `Pageable`. All list and search methods in my services use `PageableUtils` so the same logic is reused. I also wrote `PaginationPerformanceTest` to confirm that `Pageable` is passed correctly and that we rely on DB-level pagination.”

---

## Epic 3 – Transactions & Optimization (Summary)

- **`@Transactional` on write methods**
  - In `PostService`, `CommentService`, `UserService`, `TagService`, `ReviewService`: `create`, `update`, `delete` are annotated with `@Transactional` to ensure all-or-nothing updates.

- **Propagation & rollback understanding**
  - Default propagation `REQUIRED` is used on services – they join or start transactions automatically.
  - `TransactionRollbackTest` sets `@Transactional(propagation = Propagation.NOT_SUPPORTED)` on the test class and uses `TransactionTemplate` to:
    - Start a transaction.
    - Call `commentService.create(...)`.
    - Throw a `RuntimeException` and assert the DB state is unchanged (rollback verified).

- **Query optimization & indexes**
  - Complex JPQL is in repositories (search by keyword, username, etc.).
  - Entities declare indexes with `@Table(indexes = {...})` for frequently used columns (e.g. `Post` title, createdAt, userId; `User` username, email; etc.).
  - `PerformanceMonitoringAspect` logs execution time of service methods, so you can compare query performance.

**How to present:**  
“All my write operations run inside transactions with default propagation. I wrote an integration test that proves rollback when an exception occurs. For performance, I optimized queries via JPQL and added indexes on the columns used for search and sorting, then used an AOP aspect to log method execution times.”

---

## Epic 4 – Caching & Performance

- **Caching enabled globally**
  - `BloggingApiApplication` is annotated with `@EnableCaching`.

- **Cache configuration**
  - `application-dev.properties` sets `spring.cache.type=simple` and defines cache names: `posts,users,tags`.

- **Cache usage in services**
  - `PostService`, `UserService`, `TagService`:
    - `@Cacheable(value = "posts/users/tags", key = "#id")` on `getById(...)` to cache reads.
    - `@CacheEvict(value = "posts/users/tags", allEntries = true)` on create/update/delete to invalidate stale data.

- **Measuring improvement**
  - You can demonstrate:
    - First `GET /posts/{id}` hits the DB, second one is served from cache.
    - After `POST /posts`, the cache is evicted so subsequent `GET` returns fresh data.

**How to present:**  
“I enabled Spring Cache on the main application and configured simple caches for posts, users, and tags. Reads by ID use `@Cacheable`, and any write operation clears the related cache with `@CacheEvict`. This reduces DB load for frequently accessed entities.”

---

## Epic 5 – Reporting & Documentation

- **Repository and query documentation**
  - The main `README.md` describes repository structure, query types (derived, JPQL, native), indexes, pagination, and sorting.

- **Transaction strategy**
  - The “Transaction Handling” section in `README.md` explains:
    - `@Transactional` on write methods.
    - Default propagation (`REQUIRED`) and rollback on runtime exceptions.

- **Caching documentation**
  - The “Caching” section in `README.md` documents:
    - `@EnableCaching`, cache names, and how `@Cacheable`/`@CacheEvict` are applied.
    - How to verify cache hits vs misses.

**How to present:**  
“I documented how repositories, queries, transactions, and caching work in the main README and additional docs. That makes it easier for someone else to understand and maintain the data layer.”

---

## One-Sentence Summary You Can Tell Your Mentor

“In this phase I used Spring Data JPA repositories with derived and custom queries, added pagination and sorting with `Pageable`, managed data consistency with `@Transactional` and explicit rollback tests, optimized queries with indexes and JPQL, and boosted performance using Spring Cache, all documented in the project README and docs.”

