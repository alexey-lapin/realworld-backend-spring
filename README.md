# ![RealWorld Example App using Spring](example-logo.png)

> ### Spring Boot codebase containing real world examples (CRUD, auth, advanced patterns, etc) that adheres to the [RealWorld](https://github.com/gothinkster/realworld) spec and API

[![CI](https://github.com/alexey-lapin/realworld-backend-spring/workflows/CI/badge.svg)](https://github.com/alexey-lapin/realworld-backend-spring/actions)
[![Codecov](https://img.shields.io/codecov/c/gh/alexey-lapin/realworld-backend-spring?logo=codecov)](https://codecov.io/gh/alexey-lapin/realworld-backend-spring)

This codebase was created to demonstrate a fully fledged fullstack application built with **[Spring Boot](https://spring.io/projects/spring-boot)** including CRUD operations, authentication, routing, pagination, and more.

We've gone to great lengths to adhere to the Spring community styleguides & best practices.

For more information on how to this works with other frontends/backends, head over to the [RealWorld](https://github.com/gothinkster/realworld) repo.

Actual application is accessible on **Heroku**: https://realworld-backend-spring.herokuapp.com/api

# How it works
This application basically uses Spring Boot Framework with Java 8 with some other modules:
- [Spring Data](https://spring.io/projects/spring-data-jpa) with Hibernate
- Json Web Token [jjwt](https://github.com/jwtk/jjwt)
- H2 in memory database

Some other highlights:
- [Github Actions](https://github.com/alexey-lapin/realworld-backend-spring/actions) as CI
- Execution of [Realworld Postman collection](https://github.com/gothinkster/realworld/blob/master/api/Conduit.postman_collection.json) is part of CI
- Deployment on [Heroku](https://realworld-backend-spring.herokuapp.com/api) is part of CI

# Getting started
Java 8 or above is required

    ./gradlew bootRun

To test that it works, open a browser tab at http://localhost:8080/api/tags .  
Alternatively, you can run

    curl http://localhost:8080/api/tags

# Try it out with a RealWorld frontend

The entry point address of the backend API is at http://localhost:8080/api

# Run test

The repository contains a lot of test cases to cover both api test and repository test.

    ./gradlew test
