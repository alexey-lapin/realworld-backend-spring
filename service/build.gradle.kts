import org.springframework.boot.gradle.tasks.bundling.BootBuildImage
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.spring.boot)
    id("realworld.java-conventions")
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
    runtimeOnly("org.liquibase:liquibase-core")

    testAnnotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    intTestAnnotationProcessor("org.projectlombok:lombok")
    intTestCompileOnly("org.projectlombok:lombok")
}

tasks {
    named<BootJar>("bootJar") {
        archiveFileName.set("${rootProject.name}-${archiveVersion.get()}.${archiveExtension.get()}")
    }

    named<BootBuildImage>("bootBuildImage") {
        val registry = System.getenv("CR_REGISTRY")!!
        val namespace = System.getenv("CR_NAMESPACE")!!
        imageName = "${registry}/${namespace}/${rootProject.name}:${project.version}"
        publish = true
        tags = setOf("${registry}/${namespace}/${rootProject.name}:latest")
        docker {
            publishRegistry {
                url = System.getenv("CR_REGISTRY")
                username = System.getenv("CR_USERNAME")
                password = System.getenv("CR_PASSWORD")
            }
        }
    }
}
