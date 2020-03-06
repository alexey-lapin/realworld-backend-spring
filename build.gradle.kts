plugins {
    java
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("com.github.ben-manes.versions")
}

repositories {
    mavenCentral()
}

dependencies {
    annotationProcessor("org.projectlombok:lombok")
    compileOnly("org.projectlombok:lombok")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")

    implementation("io.jsonwebtoken:jjwt-api:${Versions.jwt}")

    runtimeOnly("com.h2database:h2")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:${Versions.jwt}")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:${Versions.jwt}")

    testAnnotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    withType<Test> {
        useJUnitPlatform()
    }
}