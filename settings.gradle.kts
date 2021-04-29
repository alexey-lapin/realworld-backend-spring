pluginManagement {
    plugins {
        id("com.diffplug.spotless") version "5.12.4"
        id("com.github.ben-manes.versions") version "0.38.0"
        id("org.springframework.boot") version "2.4.5"
        id("io.spring.dependency-management") version "1.0.11.RELEASE"
    }
}

rootProject.name = "realworld-backend-spring"
include("service-bus")
include("service-api")
include("service")
