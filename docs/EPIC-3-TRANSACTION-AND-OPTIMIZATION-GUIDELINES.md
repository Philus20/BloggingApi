# Epic 3: Transaction Management and Optimization — Presentation Guidelines

Use this document to present to your mentor how you fulfilled **Epic 3** (User Stories 3.1 and 3.2).

---

## User Story 3.1: Data Consistency During Post and Comment Updates

**Goal:** Ensure data consistency during post and comment updates so that no data loss occurs during concurrent transactions.

### Acceptance Criteria & How You Fulfilled Them

#### 1. `@Transactional` applied to service methods handling create/update/delete

**Where to show in codebase:**

| Service | Create | Update | Delete |
|---------|--------|--------|--------|
| **PostService** | `create()` — line 29 | `update()` — line 38 | `delete()` — line 47 |
| **CommentService** | `create()` — line 31 | `update()` — line 42 | `delete()` — line 50 |
| **UserService** | `create()` — line 25 | `update()` — line 32 | `delete()` — line 41 |
| **TagService** | `create()` — line 25 | `update()` — line 32 | `delete()` — line 41 |
| **ReviewService** | `create()` — line 32 | `update()` — line 41 | `delete()` — line 51 |

**Files to open:**  
`PostService.java`, `CommentService.java` — these are the ones explicitly mentioned (post and comment). You can also show `UserService`, `TagService`, and `ReviewService` to demonstrate consistency across all write operations.

**What to say:**  
“All create, update, and delete operations in the service layer are annotated with `@Transactional`. This ensures each write runs in a single database transaction so that either all changes commit or none do, avoiding partial updates and data loss under concurrency.”

---

#### 2. Proper understanding and use of transaction propagation and isolation

**Where it’s documented:**  
- **README.md** (Transaction Handling section): states that propagation is the default **REQUIRED** (join existing transaction or create a new one) and that rollback happens on unchecked exceptions.

**What to say:**  
“We use the default propagation `REQUIRED`: if the controller or another service already started a transaction, the service method joins it; otherwise Spring starts a new one. We rely on default isolation (typically READ_COMMITTED in Spring/JPA), which is appropriate for our use case. We didn’t need NESTED or REQUIRES_NEW because we don’t have special sub-transaction requirements; REQUIRED keeps the model simple and consistent.”

**Optional:**  
If asked, you can mention that the **rollback test** explicitly uses `Propagation.NOT_SUPPORTED` so the test runs *outside* a transaction and can observe that the service’s own transaction is rolled back when an exception is thrown.

---

#### 3. Rollback behavior verified during error scenarios

**Where to show:**  
- **Test:** `src/test/java/com/example/BloggingApi/Application/Transactions/TransactionRollbackTest.java`
- **Scenario:** A comment is created inside a transaction, then an unchecked exception is thrown. The test asserts that the comment count in the database is unchanged after the rollback.

**What to say:**  
“We have an integration test that verifies rollback. It uses `TransactionTemplate` to run `CommentService.create()` and then throws a `RuntimeException`. Because the service method is `@Transactional`, the transaction is rolled back. The test checks that the comment count before and after is the same, so no partial data is left in the database. This confirms that on failure we get all-or-nothing behavior and no data loss.”

**Key code to point to:**  
- The test method `createComment_WhenExceptionThrownAfterSave_ShouldRollbackAndLeaveDatabaseUnchanged()`.
- The use of `@Transactional(propagation = Propagation.NOT_SUPPORTED)` on the test class so the test itself doesn’t run in a transaction and can observe the rollback.

---

## User Story 3.2: Optimize Complex Queries for Better Response Times

**Goal:** Optimize complex queries so that system response times are improved.

### Acceptance Criteria & How You Fulfilled Them

#### 1. Complex JPQL queries optimized for performance

**Where to show:**  
Repositories use a mix of **derived methods** and **explicit JPQL** so that:

- Queries are expressed in one place and are easy to tune.
- Pagination is applied at the database level via `Pageable` (no full-table load in memory).

**Relevant repositories and queries:**

| Repository | Query type | Purpose |
|------------|------------|--------|
| **PostRepository** | `searchByKeyword` (JPQL) | Search title or content with `LIKE`; used with `Pageable`. |
| **PostRepository** | `findByAuthorUsernameContainingIgnoreCase` (JPQL) | Filter by author username with `LOWER`/`CONCAT` for case-insensitive search. |
| **PostRepository** | `countByAuthorId` (native) | Count by author; comment in code notes that `user_id` index is used. |
| **CommentRepository** | `findByAuthorUsernameContainingIgnoreCase` (JPQL) | Filter comments by author username. |
| **UserRepository** | `searchByKeyword` (JPQL) | Search username or email. |
| **ReviewRepository** | `findByUserUsernameContainingIgnoreCase` (JPQL) | Filter reviews by user username. |

**Files to open:**  
`PostRepository.java`, `CommentRepository.java` — show the `@Query` JPQL and the comment in `PostRepository` that references the index.

**What to say:**  
“We use JPQL for search and filter operations so we can control the exact query and combine conditions (e.g. title OR content, username OR email). All list/search methods take a `Pageable` so the database does pagination and we don’t load large result sets into memory. For author count we use a focused native query that can use the index on `user_id`.”

---

#### 2. Indexes validated for frequently used queries

**Where to show:**  
- **JPA entities** define indexes via `@Table(indexes = { ... })`, so they are created with the schema and align with how we query.

**Entity indexes:**

| Entity | Indexes | Supports |
|--------|---------|----------|
| **Post** | `idx_post_title`, `idx_post_created_at`, `idx_post_user_id` | Title search, sort by date, filter/count by author |
| **Comment** | `idx_comment_created_at` | Sort/filter by creation time |
| **User** | `idx_user_username`, `idx_user_email`, `idx_user_created_at` | Login/search by username or email, sort by date |
| **Tag** | `idx_tag_name` | Lookup by name, unique checks |
| **Review** | `idx_review_rating`, `idx_review_comment` | Filter by rating or comment text |

**Files to open:**  
`Domain/Post.java`, `Domain/Comment.java`, `Domain/User.java` — show the `@Table(name = "...", indexes = { ... })` block.

**Optional:**  
There is also `src/Schema/database_schema_optimized.sql` (and `src/main/resources` schema if present) with additional index ideas (e.g. full-text, composite). You can say: “We have a separate SQL schema script that documents further index options for full-text or heavier workloads; the main application uses the JPA-defined indexes for the current query patterns.”

**What to say:**  
“We validated that the most frequent access paths have indexes: post by title and created_at and user_id, comment by created_at, user by username and email, tag by name, review by rating and comment. These are declared on the entities so Hibernate/JPA creates them with the schema and they match our repository methods.”

---

#### 3. Query execution times recorded before and after optimization

**Where to show:**  
- **AOP:** `src/main/java/com/example/BloggingApi/AOP/PerformanceMonitoringAspect.java`  
  - Wraps all service layer methods and logs execution time in milliseconds (e.g. “Method X executed in Y ms”).  
  - So any call that triggers repository queries (getById, search, getAll, etc.) has its total execution time recorded in logs.

**What to say:**  
“We record execution time for all service methods using an AOP aspect. Every call that hits the database is logged with its duration in milliseconds. We can run the same operation before and after changing a query or adding an index and compare log lines to see the impact. In tests we also have a pagination test that asserts the call completes within a reasonable time when using a mocked repository, documenting that we rely on DB-level pagination rather than in-memory processing.”

**Optional:**  
- Enable SQL logging in `application-dev.properties` (e.g. `spring.jpa.show-sql=true` or logging for `org.hibernate.SQL`) to see the actual queries.  
- Run a few search/list endpoints and show the “Method … executed in X ms” log lines as “after” evidence; you can describe that “before” was either a previous run without indexes or a heavier query design.

---

## Short Summary for Your Mentor

- **3.1 Consistency:** All create/update/delete in services (including post and comment) are `@Transactional` with default propagation; rollback on runtime exceptions is verified by `TransactionRollbackTest`.  
- **3.2 Queries:** JPQL and native queries are used in repositories with pagination; entity indexes are defined and aligned with frequent queries; execution times are recorded via `PerformanceMonitoringAspect` so you can compare before/after optimization.

Use the file paths and table references above to navigate the codebase during the presentation.
