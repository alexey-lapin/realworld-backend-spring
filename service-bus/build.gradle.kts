plugins {
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.spring.boot)
    id("realworld.java-conventions")
}

dependencies {
    annotationProcessor("org.projectlombok:lombok")
    compileOnly("org.projectlombok:lombok")

    implementation("org.springframework:spring-context")
}

tasks {
    bootJar {
        enabled = false
    }

    jar {
        enabled = true
    }
}
