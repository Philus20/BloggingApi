# BloggingApi Project Setup & Development Guide

## 📋 Project Overview

This is a **Spring Boot 3.2.2** RESTful API for a blogging platform built with **Java 21** using **PostgreSQL** database.

### 🏗️ Architecture
```
com.example.BloggingApi/
├── API/                    # Controllers, DTOs, Validation
├── Application/              # Commands, Queries, Services
├── Domain/                  # Entities (Models)
├── Infrastructure/           # Repositories, Database, Factories
└── Security/                # Security Configuration
```

### 🛠️ Technology Stack
- **Framework**: Spring Boot 3.2.2
- **Language**: Java 21
- **Database**: PostgreSQL 42.7.7
- **Build Tool**: Maven
- **API Documentation**: Swagger/OpenAPI 2.2.0
- **Validation**: Jakarta Validation
- **Testing**: JUnit 5

---

## 🚀 Quick Setup Guide

### 1. Prerequisites
```bash
# Required Software
- Java 21+
- Maven 3.6+
- PostgreSQL 12+
- IDE (IntelliJ IDEA recommended)
```

### 2. Database Setup
```sql
-- Create Database
CREATE DATABASE springDb;

-- Create User (or use existing)
CREATE USER philus WITH PASSWORD 'philus';
GRANT ALL PRIVILEGES ON DATABASE springDb TO philus;
```

### 3. Environment Variables
Set these environment variables:
```bash
# Windows (Command Prompt)
set DB_USERNAME=philus
set DB_PASSWORD=philus

# Windows (PowerShell)
$env:DB_USERNAME="philus"
$env:DB_PASSWORD="philus"

# Linux/Mac
export DB_USERNAME=philus
export DB_PASSWORD=philus
```

### 4. Database Schema
The project uses **snake_case** column names in database:

```sql
-- Users Table
CREATE TABLE users (
    user_id BIGSERIAL PRIMARY KEY,
    user_name VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Posts Table
CREATE TABLE posts (
    post_id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    author_id BIGINT REFERENCES users(user_id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Comments Table
CREATE TABLE comments (
    comment_id BIGSERIAL PRIMARY KEY,
    content TEXT NOT NULL,
    author_id BIGINT REFERENCES users(user_id) ON DELETE CASCADE,
    post_id BIGINT REFERENCES posts(post_id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tags Table
CREATE TABLE tags (
    tag_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Reviews Table
CREATE TABLE reviews (
    review_id BIGSERIAL PRIMARY KEY,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    content TEXT,
    author_id BIGINT REFERENCES users(user_id) ON DELETE CASCADE,
    post_id BIGINT REFERENCES posts(post_id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 🏃‍♂️ Running the Application

### Option 1: Using Maven Wrapper
```bash
# Navigate to project root
cd "c:\Users\TheophilusQuaicoe\OneDrive - AmaliTech gGmbH\Desktop\Java Projects Folder\BloggingApi"

# Run application
.\mvnw spring-boot:run
```

### Option 2: Using Maven
```bash
mvn spring-boot:run
```

### Option 3: Run from IDE
1. Open `BloggingApiApplication.java`
2. Right-click → Run 'BloggingApiApplication.main()'

### Application will start on:
- **URL**: http://localhost:8080
- **API Docs**: http://localhost:8080/swagger-ui.html
- **GraphQL**: http://localhost:8080/graphql

---

## 📁 Key Project Files & Locations

### 🎯 Main Entry Point
```
src/main/java/com/example/BloggingApi/BloggingApiApplication.java
```

### 🔧 Configuration Files
```
src/main/resources/
├── application.properties          # Main config
├── application-dev.properties     # Dev environment
└── graphql/                    # GraphQL schemas
```

### 🌐 API Controllers
```
src/main/java/com/example/BloggingApi/API/Controllers/RestControllers/
├── UserController.java           # User endpoints
├── PostController.java           # Post endpoints
├── CommentController.java        # Comment endpoints
└── TagController.java           # Tag endpoints
```

### 📊 Database Layer
```
src/main/java/com/example/BloggingApi/Infrastructure/Persistence/Database/
├── Repositories/               # Data access layer
│   ├── UserRepository.java
│   ├── PostRepository.java
│   ├── CommentRepository.java
│   └── TagRepository.java
└── factories/
    └── ConnectionFactory.java    # Database connection
```

### 🏷️ Domain Models
```
src/main/java/com/example/BloggingApi/Domain/Entities/
├── User.java
├── Post.java
├── Comment.java
├── Tag.java
└── Review.java
```

### 📝 Request/Response Objects
```
src/main/java/com/example/BloggingApi/API/
├── Requests/                   # Input DTOs
│   ├── CreateUserRequest.java
│   ├── EditUserRequest.java
│   └── CreatePostRequest.java
└── Resposes/                   # Output DTOs
    ├── UserResponse.java
    └── PostResponse.java
```

---

## 🔧 Common Development Tasks

### Adding New Entity
1. Create entity in `Domain/Entities/`
2. Create repository in `Infrastructure/Persistence/Database/Repositories/`
3. Create request/response DTOs in `API/`
4. Create controller in `API/Controllers/RestControllers/`
5. Add validation annotations as needed

### Database Connection Issues
Check `ConnectionFactory.java`:
```java
String url = "jdbc:postgresql://localhost:5432/springDb";
String username = "philus";
String password = "philus";
```

### Validation Issues
- Custom validators in `API/Validation/`
- Entity annotations in `Domain/Entities/`
- Request DTOs in `API/Requests/`

---

## 🐛 Common Issues & Solutions

### Issue: "Column name id was not found"
**Cause**: Mismatch between database column names and Java mapping
**Solution**: Ensure database uses snake_case (user_id, post_id) and repositories use correct column names

### Issue: "FATAL: password authentication failed"
**Cause**: Wrong database credentials
**Solution**: Check environment variables and database user/password

### Issue: "No validator could be found for constraint"
**Cause**: Wrong validation annotation on field type
**Solution**: Remove @NotBlank from Long fields, use appropriate annotations

---

## 🧪 Testing

### Run Tests
```bash
# All tests
mvn test

# Specific test class
mvn test -Dtest=UserRepositoryTest
```

### Test Structure
```
src/test/java/com/example/BloggingApi/
├── Application/Commands/        # Command tests
├── API/Controllers/           # Controller tests
└── Infrastructure/             # Repository tests
```

---

## 📚 API Endpoints

### Users
- `GET /users` - Get all users (paginated)
- `GET /users/{id}` - Get user by ID
- `POST /users` - Create user
- `PUT /users` - Update user
- `DELETE /users/{id}` - Delete user

### Posts
- `GET /posts` - Get all posts (paginated)
- `GET /posts/{id}` - Get post by ID
- `POST /posts` - Create post
- `PUT /posts` - Update post
- `DELETE /posts/{id}` - Delete post

### Comments
- `GET /comments` - Get all comments (paginated)
- `POST /comments` - Create comment
- `PUT /comments` - Update comment
- `DELETE /comments/{id}` - Delete comment

---

## 🔍 Debugging Tips

### Enable Debug Logging
Add to `application-dev.properties`:
```properties
logging.level.com.example.BloggingApi=DEBUG
logging.level.org.springframework.jdbc=DEBUG
```

### Common Breakpoints
- `ConnectionFactory.createConnection()` - Database connection issues
- `Repository.mapResultSetTo*()` - Data mapping issues
- `Controller` methods - Request/response issues

---

## 🚀 Building for Production

```bash
# Clean and package
mvn clean package

# Run tests and package
mvn clean package -DskipTests=false

# Create executable JAR
mvn clean package spring-boot:repackage
```

Output: `target/BloggingApi-0.0.1-SNAPSHOT.jar`

---

## 📞 Team Collaboration Notes

### Git Workflow
```bash
# Feature branch
git checkout -b feature/new-endpoint
git add .
git commit -m "Add new user endpoint"
git push origin feature/new-endpoint
# Create pull request
```

### Code Style
- Use snake_case for database columns
- Use camelCase for Java fields
- Add validation annotations to request DTOs
- Keep business logic in Application layer
- Keep data access in Infrastructure layer

### Environment Management
- **Dev**: Uses `application-dev.properties`
- **Prod**: Uses `application.properties`
- Set `spring.profiles.active` accordingly

---

## 🆘️ Getting Help

### Check Logs
```bash
# Application logs
tail -f logs/application.log

# Maven build logs
mvn clean compile -X
```

### Common Port Issues
- **8080**: Default Spring Boot port
- **5432**: PostgreSQL port
- Check firewall if connection refused

### Database Connection Test
```sql
-- Test connection
\c springDb philus
-- Should show: You are now connected to database "springDb" as user "philus"
```

---

*Last Updated: February 2026*
*For team collaboration and quick onboarding*
