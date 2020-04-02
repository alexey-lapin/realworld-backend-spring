pluginManagement {
    plugins {
        id("com.github.ben-manes.versions") version "0.28.0"
        id("com.diffplug.gradle.spotless") version "3.27.0"
        id("org.springframework.boot") version "2.2.6.RELEASE"
        id("io.spring.dependency-management") version "1.0.9.RELEASE"
    }
}

rootProject.name = "realworld-spring-boot"
include("service-bus")
include("service-api")
include("service")
