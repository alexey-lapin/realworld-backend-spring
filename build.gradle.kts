import com.github.benmanes.gradle.versions.updates.resolutionstrategy.ComponentSelectionWithCurrent

plugins {
    alias(libs.plugins.versions)
    id("realworld.jacoco-aggregation")
}

description = "Real world backend API built in Spring Boot"

dependencies {
    implementation(project(":service"))
}

tasks {
    dependencyUpdates {
        checkConstraints = true
        resolutionStrategy {
            componentSelection {
                all { selection: ComponentSelectionWithCurrent ->
                    val rejected = listOf("alpha", "beta", "rc", "cr", "m", "preview", "b", "ea")
                        .map { qualifier -> Regex("(?i).*[.-]$qualifier[.\\d-+]*") }
                        .any { it.matches(selection.candidate.version) }
                    if (rejected) {
                        selection.reject("Release candidate")
                    }
                }
            }
        }
    }
}
