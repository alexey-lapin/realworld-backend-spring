# ![RealWorld Example App using Spring](example-logo.png)

> **A Spring Boot implementation of the [RealWorld](https://github.com/gothinkster/realworld) API specification.**
>
> Built with modern Java 25, Spring Boot 4, and GraalVM native image support for instant startup and minimal resource
> footprint.

[![CI](https://github.com/alexey-lapin/realworld-backend-spring/workflows/CI/badge.svg)](https://github.com/alexey-lapin/realworld-backend-spring/actions)
[![Codecov](https://codecov.io/gh/alexey-lapin/realworld-backend-spring/branch/master/graph/badge.svg)](https://codecov.io/gh/alexey-lapin/realworld-backend-spring)

This implementation goes beyond a simple demo. It's a production-ready backend featuring modern Spring Boot
architecture, GraalVM native compilation, comprehensive testing, and automated CI/CD pipelines. Every design decision
prioritizes maintainability, performance, and developer experience.

Built with **[Spring Boot](https://spring.io/projects/spring-boot)** 4 and the latest Java features, this project
demonstrates real-world patterns including JWT authentication, CQRS-inspired command/query separation, database
versioning with Liquibase, and multi-platform native executable generation.

This backend fully implements the [RealWorld](https://github.com/gothinkster/realworld) specification and can work with
any RealWorld frontend.

## Live Demo

Check out the live application on [**Render**](https://render.com/):

| Resource   | URL                                                                 |
|------------|---------------------------------------------------------------------|
| api        | https://realworld-backend-spring.onrender.com/api                   |
| swagger-ui | https://realworld-backend-spring.onrender.com/swagger-ui/index.html |

💡 The application is deployed on a free tier, so it may take a few seconds to start.

# How it works

### Architecture & Technologies

**Modern Spring Stack:**

- [Spring Boot 4](https://docs.spring.io/spring-boot/index.html)
- [Spring Data JDBC](https://docs.spring.io/spring-data/relational/reference/jdbc.html) for lightweight data access
- [Spring Security OAuth2 Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html)
  with JWT authentication
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/reference/actuator/index.html) for monitoring and management
- [Springdoc OpenAPI](https://springdoc.org) for interactive API documentation

**Application Design:**

- Multi-module Gradle build with Kotlin DSL
- Command/Query Bus pattern for CQRS-inspired separation of concerns
- Database aggregate views for optimized read operations
- Modern Java features: records, sealed interfaces, pattern matching
- [MapStruct](https://mapstruct.org/) with Spring integration for type-safe DTO mapping
- [JSpecify](https://jspecify.dev/) nullability annotations for enhanced type safety

**Data & Infrastructure:**

- [Liquibase](https://docs.liquibase.com/home.html) for versioned database schema management
- H2 in-memory database for development and testing
- [Spring Declarative HTTP Clients](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html#rest-http-interface)
  for integration tests

**Native Compilation:**

- Full GraalVM native image support with optimized runtime hints
- Multi-platform builds: Linux (AMD64/ARM64), macOS (ARM64), Windows
- Native Docker images with minimal [Wolfi](https://images.chainguard.dev/directory/image/wolfi-base/overview) base for
  production deployment

### CI/CD Pipeline

The project features a comprehensive automated pipeline:

- Multi-platform builds: JVM JAR + native executables for 4 platforms
- Automated testing with JUnit and integration tests
- RealWorld spec compliance verification
  via [Postman collection](https://github.com/gothinkster/realworld/blob/master/api/Conduit.postman_collection.json)
  with newman
- Code coverage tracking with [Codecov](https://codecov.io/gh/alexey-lapin/realworld-backend-spring)
- Docker image building and publishing
  to [GitHub Container Registry](https://github.com/alexey-lapin/realworld-backend-spring/pkgs/container/realworld-backend-spring)
- Automated [GitHub releases](https://github.com/alexey-lapin/realworld-backend-spring/releases) with platform-specific
  artifacts
- Continuous deployment to [Render](https://realworld-backend-spring.onrender.com/swagger-ui/index.html)

## Getting started

**Requirements:** Java 17 or above

Choose your preferred way to run the application:

### JVM Mode

Traditional Spring Boot application with the full JVM:

```bash
./gradlew run
```

Or download the JAR from the [releases](https://github.com/alexey-lapin/realworld-backend-spring/releases/latest) page:

```bash
java -jar realworld-backend-spring-*.jar
```

### Native Image Mode

GraalVM native executable for instant startup and minimal memory footprint:

```bash
./gradlew nativeRun
```

Or download the pre-built native executable for your platform from
the [releases](https://github.com/alexey-lapin/realworld-backend-spring/releases/latest) page:

```bash
./realworld-backend-spring
```

Available platforms: Linux (AMD64/ARM64), macOS (ARM64), Windows

### Docker

Run the containerized native image:

```bash
docker run -p 8080:8080 ghcr.io/alexey-lapin/realworld-backend-spring:latest
```

The application will be available at:

- **API:** http://localhost:8080/api
- **Swagger UI:** http://localhost:8080/swagger-ui/index.html

## Frontend Integration

This backend implements the complete RealWorld API specification and works seamlessly with
any [RealWorld frontend](https://github.com/gothinkster/realworld).

**API Base URL:** `http://localhost:8080/api`

Point your frontend to this endpoint and you're ready to go. All authentication, CRUD operations, pagination, and
filtering are fully supported.

## Testing

The project includes comprehensive test coverage with unit tests, integration tests, and RealWorld spec compliance
testing:

```bash
# Run all tests
./gradlew test

# Run integration tests
./gradlew integrationTest

# Build and run newman tests
./gradlew build
```

Test suite includes:

- Unit tests for business logic and handlers
- Integration tests using Spring's declarative HTTP clients
- RealWorld Postman collection validation
- Code coverage reporting via JaCoCo and Codecov

### Manual API Testing

For interactive API exploration and testing, use the included IntelliJ IDEA HTTP Client file:

**File:** `api.http`

This file contains ready-to-use requests for all API endpoints:

- User registration and authentication
- Profile management (follow/unfollow)
- Article CRUD operations with feed and filtering
- Comments and favorites
- Tags listing

Open the file in IntelliJ IDEA or any compatible IDE and execute requests directly. The file uses environment variables
and response handlers to chain requests automatically (e.g., capturing tokens for authenticated requests).

### Performance Testing

Load test the application with the included k6 script:

```bash
k6 run k6-create-articles.js
```

The script simulates realistic article creation workload:

- 5 virtual users for 30 seconds
- Performance thresholds: <1% error rate, p95 latency <750ms
- Automatic user registration and JWT token management
- Random tag generation for realistic data distribution

Customize the test by setting the `BASE_URL` environment variable:

```bash
k6 run -e BASE_URL=https://your-api.com/api k6-create-articles.js
```
