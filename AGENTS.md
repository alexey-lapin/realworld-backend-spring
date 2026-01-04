# AGENTS

This file guides agentic coding assistants working in this repository.
It summarizes verified build/test commands and code conventions.

## Project overview

Spring Boot codebase containing real world examples (CRUD, auth, advanced patterns, etc) that adheres to
the *RealWorld* spec and API.

Links:
- https://github.com/gothinkster/realworld/blob/main/api/openapi.yml

## Scope and modules

- Root project: Gradle multi-module build using Java 17.
- Modules: 
  - `service` (Spring Boot app), 
  - `service-api` (DTOs/commands/operations),
  - `service-bus` (command/query bus abstractions).
- Gradle conventions live in `gradle/plugins/common`.

## Build, run, lint, test

Use the Gradle wrapper from the repo root.

Build and verify (all modules):

```bash
./gradlew build
```

Run the app (JVM):

```bash
./gradlew bootRun
```

Run the native image (requires GraalVM):

```bash
./gradlew nativeRun
```

Run unit tests only:

```bash
./gradlew test
```

Run integration tests (defined in `service/src/intTest`):

```bash
./gradlew integrationTest
```

Full verification (unit + integration + Spotless):

```bash
./gradlew check
```

Code formatting / lint (Spotless):

```bash
./gradlew spotlessCheck
./gradlew spotlessApply
```

Aggregated coverage report:

```bash
./gradlew jacocoReport
```

### Run a single test

Use Gradle `--tests` to target a class or method.

Single unit test class:

```bash
./gradlew :service:test --tests "com.github.al.realworld.application.service.SlugServiceTest"
```

Single test method:

```bash
./gradlew :service:test --tests "com.github.al.realworld.application.service.SlugServiceTest.name1"
```

Single integration test class:

```bash
./gradlew :service:integrationTest --tests "com.github.al.realworld.rest.ArticleApiTest"
```

Tip: prefix with `:service:` when iterating locally to avoid running all modules.

## Code style and conventions

### Source headers and formatting

- All Java sources include the MIT license header from `src/spotless/mit-license.java`.
- Spotless enforces: license header, no unused imports, trimmed trailing whitespace,
  and newline at EOF. It does not auto-reformat code layout.
- Use 4-space indentation, braces on the same line, and keep line breaks consistent
  with nearby code.
- Prefer modern Java idioms: records, pattern matching, sealed interfaces/classes, var for local variables.
- Annotate the code with nullability annotations (`org.jspecify.annotations.Nullable`) where necessary

### Imports

- Avoid wildcard imports; Spotless removes unused imports.

### Packaging and layering

- API DTOs, commands, queries, and operations are in `service-api`.
- Application logic lives under `service/src/main/java/.../application`.
    - `command` handlers mutate state; `query` handlers read state.
- Domain models and repositories live under `domain`.
- Infrastructure adapters (web, db/jdbc, converters, config) live under `infrastructure`.
- Keep dependencies flowing inward (infrastructure -> application -> domain).

### Naming conventions

- Commands/queries are imperative: `CreateArticle`, `GetFeed`.
- Handlers are suffixed with `Handler`: `CreateArticleHandler`.
- Result DTOs end with `Result`: `CreateArticleResult`.
- Controllers implement `*Operations` from `service-api`.

### Spring patterns

- Prefer constructor injection.
- Annotate command handlers with `@Service` and transactional boundaries with
  `@Transactional` when writing.
- Controllers accept command/query objects and delegate to the `Bus`.
- Use `ConversionService` (and MapStruct-generated converters) for DTO mapping.

### Error handling

- Use custom exceptions in `application/exception` annotated with `@ResponseStatus`
  (e.g., `BadRequestException`, `NotFoundException`).
- Prefer helper constructors like `badRequest(...)` for formatted messages.
- Use `Optional` from repositories and `orElseThrow()` when absence is exceptional.
- Handle concurrency/uniqueness conflicts explicitly (see `DuplicateKeyException`
  handling in command handlers).

## Testing conventions

- JUnit 6 (`@Test`, `@BeforeAll`) with AssertJ assertions.
- Unit tests live in `service/src/test/java`.
- Integration tests live in `service/src/intTest/java` and run via `integrationTest`.
- Keep test data small and focus on behavior; follow existing naming patterns
  within the same test class.

## When editing

- Update or add tests for behavior changes.
- Run `./gradlew spotlessApply` and `./gradlew test` (or narrower) before review.
- Avoid touching `gradle/plugins/**/build` output artifacts.
- Keep changes minimal and consistent with nearby code.
