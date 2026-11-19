# ![RealWorld Example App using Spring](example-logo.png)

> **A Spring Boot implementation of the [RealWorld](https://github.com/realworld-apps/realworld) API specification.**
>
> Built with modern Java 25, Spring Boot 4, and GraalVM native image support. What that trade buys, and
> what it costs, is [measured](#startup-memory-and-latency).

[![CI](https://github.com/alexey-lapin/realworld-backend-spring/actions/workflows/main.yml/badge.svg?branch=master&event=push)](https://github.com/alexey-lapin/realworld-backend-spring/actions/workflows/main.yml?query=branch%3Amaster+event%3Apush)
[![Codecov](https://codecov.io/gh/alexey-lapin/realworld-backend-spring/branch/master/graph/badge.svg)](https://codecov.io/gh/alexey-lapin/realworld-backend-spring)

A complete implementation of the [RealWorld](https://github.com/realworld-apps/realworld) API spec on
**[Spring Boot](https://spring.io/projects/spring-boot)** 4 and Java 25, built and shipped as a GraalVM native image
alongside the usual JVM jar. Every release is checked against the upstream spec in CI on both, so "complete" is
machine-verified rather than asserted, and it works with any RealWorld frontend.

What it demonstrates: JWT authentication, a CQRS-inspired command and query split, Spring Data JDBC, schema versioning
with Liquibase, layered testing, and native binaries for four platforms produced on every release.

What it does not: the demo runs H2 in memory on a free instance, so read this as a worked reference rather than a
production deployment.

## Live Demo

Check out the live application on [**Render**](https://render.com/):

| Resource   | URL                                                                 |
|------------|---------------------------------------------------------------------|
| api        | https://realworld-backend-spring.onrender.com/api                   |
| swagger-ui | https://realworld-backend-spring.onrender.com/swagger-ui/index.html |

💡 The application is deployed on a free tier, so it may take a few seconds to start.

## Startup, memory and latency

Native image is the headline claim, so here is what it costs and returns on this codebase,
measured rather than asserted. Reproduce with [`benchmark/run.py`](benchmark/run.py); the raw
per-trial rows are committed under [`benchmark/results`](benchmark/results).

### Startup and idle memory

| Runtime | Startup to healthy, median [IQR] | Peak RSS while starting | Idle RSS after 30 s |
|---|---|---|---|
| JVM (`java -jar`) | 3.04 s [0.23] | 436 MiB | 359 MiB |
| Native image | 1.06 s [0.63] | 179 MiB | 178 MiB |

Twenty measured starts per runtime after two discarded warmups, alternating which one goes
first; idle is five trials each. Native starts about 2.9x faster and settles at half the
memory. It is also the less predictable of the two: its interquartile range is 59% of its
median, against 7.6% for the JVM, and its slowest start (2.43 s) is worse than the JVM's
median.

### Under fixed load

| Offered rate | Runtime | p95 | p99 | Steady RSS |
|---|---|---|---|---|
| 50 req/s | JVM | 4.14 ms | 6.44 ms | 438 MiB |
| 50 req/s | Native | 5.34 ms | 7.61 ms | 174 MiB |
| 200 req/s | JVM | 2.29 ms | 3.04 ms | 446 MiB |
| 200 req/s | Native | 4.84 ms | 12.87 ms | 175 MiB |
| 500 req/s | JVM | 2.91 ms | 3.37 ms | 623 MiB |
| 500 req/s | Native | 8.38 ms | 23.40 ms | 500 MiB |

Two 60-second runs per cell after a 30-second warmup, k6 at a fixed arrival rate so both
runtimes face identical offered load. Every cell delivered its full rate with zero errors and
zero dropped iterations.

**The warm JVM wins latency here, and its lead widens as load rises.** Its p99 *improves*
from 6.44 ms to 3.04 ms between 50 and 200 req/s — that is the JIT getting enough samples to
work with — while the native image degrades from 7.61 ms to 23.40 ms. That is the opposite of
the usual native-image pitch, and it is the trade this repo actually demonstrates: start fast
and stay small, or serve a steady tail.

Memory moves too. Under 500 req/s the native image's resident set grows from 175 MiB to
500 MiB as its collector expands under allocation pressure, so "half the memory" is an idle
property, not a loaded one.

### Build and artifact cost

| Artifact | Clean build | On disk | `gzip -9` |
|---|---|---|---|
| JVM jar | 6.2 s | 45.7 MiB | 41.1 MiB |
| Native binary | 62.3 s | 152.5 MiB | 52.1 MiB |

Native compilation costs ten times the build time. The binary is 3.3x larger on disk because
it carries its own runtime, though compressed the gap nearly closes: a jar is already a zip,
so 41.1 MiB against 52.1 MiB is what a download actually differs by.

### What these numbers are, and what they are not

Apple M4 Pro (12 cores), 24 GB, macOS 26.6.2, GraalVM CE 25.0.2 for both the JVM runs and the
native build, k6 v2.2.0 on the same host over loopback, H2 in memory. The load is synthetic:
60% article list, 20% tag list, 20% article create — heavier on writes than real browsing.

These are **host process RSS figures on macOS, not cgroup accounting**, so they describe
processes on this machine rather than containers under a memory limit. A second pass gave both
runtimes `-Xmx512m`: the JVM's idle RSS *rose* to 423 MiB, the native image's stayed near
184 MiB, and under 500 req/s the native image held 345 MiB at a p99 of 31.3 ms. Equal maximum
heap is not a 512 MiB container — it caps the Java heap alone, not metaspace, code cache,
thread stacks or direct buffers — so read that pass as an equal-heap experiment and nothing
more.

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

- Full GraalVM native image support, with hand-written runtime hints where Spring's AOT processing needs a nudge
  (`GraalConfig`)
- Multi-platform builds: Linux (AMD64/ARM64), macOS (ARM64), Windows (AMD64)
- Native Docker images for linux/amd64 and linux/arm64, on a minimal
  [Wolfi](https://images.chainguard.dev/directory/image/wolfi-base/overview) base for production deployment

### CI/CD Pipeline

The project features a comprehensive automated pipeline:

- Multi-platform builds: JVM JAR + native executables for four platforms
- Automated testing with JUnit and integration tests
- RealWorld spec compliance verification
  via the upstream [hurl collection](https://github.com/realworld-apps/realworld/tree/main/specs/api)
- Code coverage tracking with [Codecov](https://codecov.io/gh/alexey-lapin/realworld-backend-spring)
- Multi-arch Docker image building and publishing
  to [GitHub Container Registry](https://github.com/alexey-lapin/realworld-backend-spring/pkgs/container/realworld-backend-spring)
- Automated [GitHub releases](https://github.com/alexey-lapin/realworld-backend-spring/releases) with platform-specific
  artifacts
- Continuous deployment to [Render](https://realworld-backend-spring.onrender.com/swagger-ui/index.html)

## Getting started

**Requirements:** Java 25

Choose your preferred way to run the application:

### JVM Mode

Traditional Spring Boot application with the full JVM:

```bash
./gradlew bootRun
```

Or download the JAR from the [releases](https://github.com/alexey-lapin/realworld-backend-spring/releases/latest) page:

```bash
java -jar realworld-backend-spring-*.jar
```

### Native Image Mode

GraalVM native executable: about 2.9x faster to start and half the idle memory, at the cost of tail latency
under load ([numbers](#startup-memory-and-latency)):

```bash
./gradlew nativeRun
```

Or download the pre-built native executable for your platform from
the [releases](https://github.com/alexey-lapin/realworld-backend-spring/releases/latest) page:

```bash
./realworld-backend-spring
```

Available platforms: Linux (AMD64/ARM64), macOS (ARM64), Windows (AMD64)

### Docker

Run the containerized native image, published for linux/amd64 and linux/arm64:

```bash
docker run -p 8080:8080 ghcr.io/alexey-lapin/realworld-backend-spring:latest
```

The application will be available at:

- **API:** http://localhost:8080/api
- **Swagger UI:** http://localhost:8080/swagger-ui/index.html

## Frontend Integration

This backend implements the complete RealWorld API specification and works seamlessly with
any [RealWorld frontend](https://github.com/realworld-apps/realworld).

**API Base URL:** `http://localhost:8080/api`

Point your frontend to this endpoint and you're ready to go. All authentication, CRUD operations, pagination, and
filtering are fully supported.

## Testing

Three layers of tests: unit tests, integration tests, and compliance against the RealWorld spec itself.

```bash
# Unit tests
./gradlew test

# Integration tests, which are a separate suite and do not run as part of `test`
./gradlew integrationTest

# Both suites plus Spotless, the same verification the CI build runs
./gradlew check

# Verify against the RealWorld API spec (needs https://hurl.dev installed).
# The spec lives upstream; check it out into .realworld-spec, the same path CI
# uses. The revision is read from the workflow rather than repeated here, so a
# local run and a CI run cannot drift onto different specs.
SPEC_REF=$(grep -E '^  SPEC_REF:' .github/workflows/main.yml | awk '{print $2}')

# First time only:
git clone --filter=blob:none --sparse https://github.com/realworld-apps/realworld .realworld-spec
git -C .realworld-spec sparse-checkout set specs/api

# Before each run (also refreshes an existing checkout):
git -C .realworld-spec fetch origin && git -C .realworld-spec checkout --detach "$SPEC_REF"

./gradlew build
java -jar service/build/libs/realworld-backend-spring*.jar &
hurl --test --jobs 1 \
  --variable host=http://localhost:8080 \
  --variable uid=$(date +%s) \
  .realworld-spec/specs/api/hurl/*.hurl
```

`.realworld-spec` is a foreign clone with its own `.git`, so it is gitignored
and excluded from the Docker build context. It is about 2 MB.

Two things worth knowing:

- `host` is the origin **without** `/api`. The `.hurl` files append the prefix
  themselves, and upstream's own README example gets this wrong.
- **Check out `SPEC_REF`, don't just run whatever the checkout happens to be on.**
  The spec is a moving target and its requirements change: duplicate article
  titles once had to return `409`, and now must be accepted with distinct slugs.
  An old checkout still enforces rules the spec has since dropped, so tests fail
  against a service that is correct. To move to a newer spec, bump `SPEC_REF` in
  `.github/workflows/main.yml` deliberately and fix whatever it turns red.

A Bruno collection generated from the same files is available upstream if you
prefer to explore the requests interactively; the hurl files are the source of
truth and are what CI runs.

Test suite includes:

- Unit tests for business logic and handlers
- Integration tests using Spring's declarative HTTP clients
- RealWorld API spec validation with [hurl](https://hurl.dev)
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
