pluginManagement {
    plugins {
        id("com.diffplug.spotless") version "5.13.0"
        id("com.github.ben-manes.versions") version "0.39.0"
        id("org.springframework.boot") version "2.5.1"
        id("io.spring.dependency-management") version "1.0.11.RELEASE"
    }
}

rootProject.name = "realworld-backend-spring"
include("service-bus")
include("service-api")
include("service")
