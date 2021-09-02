enableFeaturePreview("VERSION_CATALOGS")

pluginManagement {
    plugins {
        id("com.diffplug.spotless") version "5.15.0"
        id("com.github.ben-manes.versions") version "0.39.0"
        id("io.spring.dependency-management") version "1.0.11.RELEASE"
        id("org.springframework.boot") version "2.5.4"
    }
}

rootProject.name = "realworld-backend-spring"
include("service-bus")
include("service-api")
include("service")
