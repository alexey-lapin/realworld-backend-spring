plugins {
    id("io.spring.dependency-management")
    id("org.springframework.boot")
    id("java-conventions")
}

dependencies {
    annotationProcessor("org.projectlombok:lombok")
    compileOnly("org.projectlombok:lombok")

    implementation(project(":service-bus"))
    implementation(project(":service-api"))

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("io.jsonwebtoken:jjwt-api:${libs.versions.jwt.get()}")

    runtimeOnly("com.h2database:h2")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:${libs.versions.jwt.get()}")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:${libs.versions.jwt.get()}")
    runtimeOnly("org.liquibase:liquibase-core:${libs.versions.liquibase.get()}")

    testAnnotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    "intTestAnnotationProcessor"("org.projectlombok:lombok")
    "intTestCompileOnly"("org.projectlombok:lombok")

    "intTestImplementation"("org.springframework.cloud:spring-cloud-starter-openfeign:${libs.versions.springFeign.get()}")
    "intTestImplementation"("io.github.openfeign:feign-jackson:${libs.versions.feign.get()}")
}
