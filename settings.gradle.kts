import dev.aga.gradle.versioncatalogs.Generator.generate

pluginManagement {
    includeBuild("gradle/plugins")
    repositories {
        gradlePluginPortal()
    }
}

plugins {
    id("dev.aga.gradle.version-catalog-generator") version("4.0.0")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
    versionCatalogs {
        generate("libs") {
            fromToml("springBootDependencies")
        }
    }
}

rootProject.name = "realworld-backend-spring"
include("service-bus")
include("service-api")
include("service")
