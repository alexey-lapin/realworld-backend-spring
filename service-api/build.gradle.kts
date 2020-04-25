plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    annotationProcessor("org.projectlombok:lombok")
    compileOnly("org.projectlombok:lombok")

    implementation(project(":service-bus"))
    implementation("com.fasterxml.jackson.core:jackson-annotations")
    implementation("jakarta.validation:jakarta.validation-api")
    implementation("org.springframework:spring-web")
}

tasks {
    bootJar {
        enabled = false
    }

    jar {
        enabled = true
    }
}