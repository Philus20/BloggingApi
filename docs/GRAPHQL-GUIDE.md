# GraphQL in This Project – Where It Is and How to Add New APIs

## 1. Where GraphQL Is Used

GraphQL is implemented in three places:

| Part | Location | Purpose |
|------|----------|---------|
| **Schema** | `src/main/resources/graphql/*.graphqls` | Defines types, queries, and mutations (the “contract” of the API). |
| **Controllers (resolvers)** | `src/main/java/.../Controllers/Graphql/*GraphQLController.java` | Map GraphQL operations to Java methods (receive arguments, call service, return data). |
| **Service layer** | `src/main/java/.../Services/*Service.java` | Contains the real business logic; used by both REST and GraphQL. |

**Configuration**

- **Dependency:** `spring-boot-starter-graphql` in `pom.xml`.
- **Endpoint:** GraphQL is exposed at **`/graphql`** (POST).
- **Playground:** GraphiQL UI at **`/graphiql`** (when enabled in `application-dev.properties`).

So: **schema** defines *what* is available, **GraphQL controllers** wire *which* Java method handles each field/operation, and **services** do the work. GraphQL and REST share the same services.

---

## 2. The Flow (Request → Response)

```
Client (e.g. GraphiQL or frontend)
    → POST /graphql with query/mutation
    → Spring GraphQL finds the right controller method by name (e.g. getPost, createPost)
    → Controller gets arguments via @Argument, calls service, returns result
    → Service uses repositories and returns domain objects (Post, User, etc.)
    → Spring GraphQL maps domain objects to the schema types and returns JSON
```

**Example:** A query `getPost(id: 1)` is handled by `PostGraphQLController.getPost(@Argument Long id)`, which calls `postService.getById(id)` and returns a `Post` that matches the `Post` type in the schema.

---

## 3. How to Add a New GraphQL API (New Entity / Resource)

Assume you want to add a new resource (e.g. **Category**). Follow these steps.

### Step 1: Define the type and operations in the schema

Create or edit files under `src/main/resources/graphql/`.

**In `types.graphqls`** – add the new type and its page type (if you use lists with pagination):

```graphql
type Category {
    id: ID!
    name: String!
    # add other fields
}

type CategoryPage {
    content: [Category!]!
    totalPages: Int!
    totalElements: Int!
    size: Int!
    number: Int!
}
```

**In `queries.graphqls`** – add queries under `type Query { ... }`:

```graphql
# Inside type Query { ... }
getCategory(id: ID!): Category
listCategories(page: Int = 0, size: Int = 5, sortBy: String = "id", ascending: Boolean = true): CategoryPage!
```

**Create or edit `mutations.graphqls`** – add mutations (create/update/delete):

```graphql
type Mutation {
    createCategory(name: String!): Category
    editCategory(id: ID!, name: String!): Category
    deleteCategory(id: ID!): String
}
```

(If you don’t have `mutations.graphqls` yet, create it; Spring GraphQL will merge all `.graphqls` files.)

### Step 2: Implement the service (business logic)

You already have a **service per domain** (e.g. `PostService`, `ReviewService`). For the new resource, add a **CategoryService** (or similar) in `src/main/java/.../Services/` with methods like:

- `getById(Long id)`
- `getAll(int page, int size, String sortBy, boolean ascending)` returning `Page<Category>`
- `create(CreateCategoryRequest request)`
- `update(EditCategoryRequest request)`
- `delete(Long id)`

This is the same pattern as `PostService` / `ReviewService`. Controllers (both REST and GraphQL) will call this service.

### Step 3: Create the GraphQL controller (resolver)

Create a class in `src/main/java/.../Controllers/Graphql/`, for example **`CategoryGraphQLController.java`**:

```java
package com.example.BloggingApi.Controllers.Graphql;

import com.example.BloggingApi.Services.CategoryService;
import com.example.BloggingApi.Domain.Category;
import org.springframework.data.domain.Page;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class CategoryGraphQLController {

    private final CategoryService categoryService;

    public CategoryGraphQLController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @QueryMapping
    public Category getCategory(@Argument Long id) {
        return categoryService.getById(id);
    }

    @QueryMapping
    public Page<Category> listCategories(@Argument int page, @Argument int size,
                                         @Argument String sortBy, @Argument boolean ascending) {
        return categoryService.getAll(page, size, sortBy, ascending);
    }

    @MutationMapping
    public Category createCategory(@Argument String name) {
        return categoryService.create(new CreateCategoryRequest(name));
    }

    @MutationMapping
    public Category editCategory(@Argument Long id, @Argument String name) {
        return categoryService.update(new EditCategoryRequest(id, name));
    }

    @MutationMapping
    public String deleteCategory(@Argument Long id) {
        categoryService.delete(id);
        return "Category with ID " + id + " deleted successfully.";
    }
}
```

**Important:** Method names must match the schema (e.g. `getCategory`, `listCategories`, `createCategory`). Arguments are bound with `@Argument`.

### Step 4: (Optional) Add REST endpoints

If you also want REST for the same resource, add a **CategoryController** in `Controllers.Rest` that uses the same **CategoryService**. REST and GraphQL share the service.

---

## 4. Summary Checklist for “Creating a New One”

1. **Schema** – In `graphql/types.graphqls`: add the new type (and `XxxPage` if needed).
2. **Schema** – In `graphql/queries.graphqls`: add queries (e.g. `getXxx`, `listXxxs`).
3. **Schema** – In `graphql/mutations.graphqls`: add mutations (e.g. `createXxx`, `editXxx`, `deleteXxx`).
4. **Service** – Implement `XxxService` with the real logic and repository calls.
5. **GraphQL controller** – Create `XxxGraphQLController` with `@QueryMapping` and `@MutationMapping` methods that call the service.
6. **REST (optional)** – Create `XxxController` in Rest package if you want REST as well.

After that, the new API is available at **`/graphql`** (and in GraphiQL at **`/graphiql`** if enabled). No extra “GraphQL adoption” config is needed beyond what you already have; just schema + controller + service.
