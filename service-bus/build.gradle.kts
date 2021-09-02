plugins {
    id("io.spring.dependency-management")
    id("org.springframework.boot")
    id("java-conventions")
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
