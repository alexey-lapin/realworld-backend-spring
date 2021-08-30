enableFeaturePreview("VERSION_CATALOGS")

pluginManagement {
    plugins {
        id("com.diffplug.spotless") version "5.14.2"
        id("com.github.ben-manes.versions") version "0.39.0"
        id("org.springframework.boot") version "2.5.4"
        id("io.spring.dependency-management") version "1.0.11.RELEASE"
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("jwt", "0.11.2")
            version("liquibase", "4.4.2")
            version("feign", "11.0")
            version("springFeign", "3.0.3")

            version("jacoco", "0.8.5")
        }
    }
}

rootProject.name = "realworld-backend-spring"
include("service-bus")
include("service-api")
include("service")
