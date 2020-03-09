plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
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
