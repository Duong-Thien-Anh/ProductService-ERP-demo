# AuthERP Service

## Introduction
AuthERP Service is a stateless authentication and authorization backend for ERP systems. It provides secure user registration, login, JWT-based access control, refresh token lifecycle management, and role-based authorization (RBAC) for protected APIs.

## Technology Stack

| Layer | Technology |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot 3.3.4 |
| Build Tool | Maven 3.9 |
| Database | PostgreSQL 18.1 |
| Security | Spring Security, JWT, BCrypt |
| API Docs | Springdoc OpenAPI (Swagger UI) |
| Containerization | Docker, Docker Compose |

## Prerequisites
Before running the service, ensure the following are installed:

- JDK 21
- Maven 3.9+
- Docker and Docker Compose
- PostgreSQL 18.1 (if running outside Docker)

## Configuration

### 1. Application Configuration
Update `src/main/resources/application.yml` with your environment values.

```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/autherp_db
    username: autherp_user
    password: ${DB_PASSWORD}

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false

security:
  jwt:
    secret: ${JWT_SECRET}
    access-token-expiration-ms: ${JWT_ACCESS_EXPIRATION_MS:900000}
    refresh-token-expiration-ms: ${JWT_REFRESH_EXPIRATION_MS:604800000}
```

### 2. Environment Variables
Set required environment variables before starting the service:

```bash
DB_PASSWORD=your_db_password
JWT_SECRET=your_very_strong_secret_key
JWT_ACCESS_EXPIRATION_MS=900000
JWT_REFRESH_EXPIRATION_MS=604800000
```

## Installation and Execution

### 1. Clone and open project directory
```bash
git clone <your-repository-url>
cd <your-project-folder>
```

### 2. Build project
```bash
mvn clean install
```

### 3. Run service locally
```bash
mvn spring-boot:run
```

Service base URL:

```text
http://localhost:8081
```

## Docker Deployment
Run the full stack using Docker Compose:

```bash
docker compose up --build
```

To stop and remove containers:

```bash
docker compose down
```

## API Documentation
After startup, access Swagger UI at:

[http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html)

## Project Structure

```text
autherp-service/
|-- src/
|   |-- main/
|   |   |-- java/
|   |   |   `-- com/example/autherp/
|   |   |       |-- AuthErpServiceApplication.java
|   |   |       |-- config/
|   |   |       |   |-- SecurityConfig.java
|   |   |       |   `-- OpenApiConfig.java
|   |   |       |-- controller/
|   |   |       |   |-- AuthController.java
|   |   |       |   `-- UserController.java
|   |   |       |-- dto/
|   |   |       |   |-- request/
|   |   |       |   `-- response/
|   |   |       |-- model/
|   |   |       |   |-- User.java
|   |   |       |   |-- Role.java
|   |   |       |   `-- RefreshToken.java
|   |   |       |-- repository/
|   |   |       |   |-- UserRepository.java
|   |   |       |   |-- RoleRepository.java
|   |   |       |   `-- RefreshTokenRepository.java
|   |   |       |-- security/
|   |   |       |   |-- JwtAuthenticationFilter.java
|   |   |       |   `-- JwtTokenProvider.java
|   |   |       `-- service/
|   |   |           |-- AuthService.java
|   |   |           |-- UserService.java
|   |   |           `-- RefreshTokenService.java
|   |   `-- resources/
|   |       |-- application.yml
|   |       `-- application-dev.yml
|   `-- test/
|       `-- java/
|-- pom.xml
`-- docker-compose.yml
```

## Security Note
Swagger supports JWT authorization via the Bearer token scheme:

1. Authenticate using the login endpoint to receive an access token.
2. Click **Authorize** in Swagger UI.
3. Enter the token in this format:

```text
Bearer <access_token>
```

4. Execute protected endpoints. Requests will include the `Authorization` header automatically.

---

If your actual package names or folder layout differ, update the **Project Structure** section to match your codebase exactly.
