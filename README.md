# ![RealWorld Example App using Spring](example-logo.png)

> ### Spring Boot codebase containing real world examples (CRUD, auth, advanced patterns, etc) that adheres to the [RealWorld](https://github.com/gothinkster/realworld) spec and API

[![CI](https://github.com/alexey-lapin/realworld-backend-spring/workflows/CI/badge.svg)](https://github.com/alexey-lapin/realworld-backend-spring/actions)
[![Codecov](https://codecov.io/gh/alexey-lapin/realworld-backend-spring/branch/master/graph/badge.svg)](https://codecov.io/gh/alexey-lapin/realworld-backend-spring)

This codebase was created to demonstrate a fully fledged fullstack application built with
**[Spring Boot](https://spring.io/projects/spring-boot)** including CRUD operations, authentication, routing,
pagination, and more.

We've gone to great lengths to adhere to the Spring community styleguides & best practices.

For more information on how to this works with other frontends/backends, head over to the
[RealWorld](https://github.com/gothinkster/realworld) repo.

## Live Demo

Check out the live application on [**Render**](https://render.com/):

| Resource   | URL                                                                 |
|------------|---------------------------------------------------------------------|
| api        | https://realworld-backend-spring.onrender.com/api                   |
| swagger-ui | https://realworld-backend-spring.onrender.com/swagger-ui/index.html |

💡 The application is deployed on a free tier, so it may take a few seconds to start.

# How it works

### App

- [Spring Boot](https://docs.spring.io/spring-boot/index.html)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/reference/jpa.html) with Hibernate
- [Liquibase](https://docs.liquibase.com/home.html)
  for the database changes management
- [Spring Security OAuth2 Resource Server JWT](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html)
  for authentication and authorization
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/reference/actuator/index.html) built-in endpoints
- [Springdoc OpenAPI/Swagger](https://springdoc.org)
- [Spring Declarative HTTP Clients](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html#rest-http-interface)
  for integration tests
- H2 in memory database
- GraalVM support

### CI

- [Github Actions](https://github.com/alexey-lapin/realworld-backend-spring/actions)
- Building jar
- Building GraalVM native image for Linux, macOS, and Windows
- Building Docker image with the native executable and pushing it to
  [GitHub Container Registry](https://github.com/alexey-lapin/realworld-backend-spring/pkgs/container/realworld-backend-spring)
- Execution of
  [Realworld Postman collection](https://github.com/gothinkster/realworld/blob/master/api/Conduit.postman_collection.json)
  with newman for all the builds
- Code coverage with [Codecov](https://codecov.io/gh/alexey-lapin/realworld-backend-spring)
- Publishing [GitHub release](https://github.com/alexey-lapin/realworld-backend-spring/releases) with the artifacts
- Triggering deployment on [Render](https://realworld-backend-spring.onrender.com/swagger-ui/index.html)

## Getting started

Note that Java 17 or above is required to build the project with Gradle.

### JVM

Run the application with Gradle:

    ./gradlew run

Or download the latest artifact from
the [releases](https://github.com/alexey-lapin/realworld-backend-spring/releases/latest) page,
and run:

    java -jar realworld-backend-spring-*.jar

### Native Image

Run the application with Gradle:

    ./gradlew nativeRun

Or download the latest artifact for your platform from
the [releases](https://github.com/alexey-lapin/realworld-backend-spring/releases/latest) page,
unpack, and run the executable:

    ./realworld-backend-spring

### Docker

Run the application with Docker:

    docker run -p 8080:8080 ghcr.io/alexey-lapin/realworld-backend-spring:latest

## Try it out with a RealWorld frontend

The entry point address of the backend API is at http://localhost:8080/api

## Run test

The repository contains a lot of test cases to cover both api test and repository test.

    ./gradlew test
