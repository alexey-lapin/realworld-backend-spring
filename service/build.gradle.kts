plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

sourceSets {
    create("intTest") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

configurations["intTestImplementation"].extendsFrom(configurations["implementation"])
configurations["intTestImplementation"].extendsFrom(configurations["testImplementation"])
configurations["intTestRuntimeOnly"].extendsFrom(configurations["runtimeOnly"])
configurations["intTestRuntimeOnly"].extendsFrom(configurations["testRuntimeOnly"])

//idea {
//    module {
//        testSourceDirs.addAll(sourceSets["intTest"].java.srcDirs)
//        testResourceDirs.addAll(sourceSets["intTest"].resources.srcDirs)
//        scopes["TEST"]!!["plus"]!!.add(configurations["intTestCompile"])
//    }
//}

dependencies {
    annotationProcessor("org.projectlombok:lombok")
    compileOnly("org.projectlombok:lombok")

    implementation(project(":service-bus"))
    implementation(project(":service-api"))

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("io.jsonwebtoken:jjwt-api:${Versions.jwt}")

    runtimeOnly("com.h2database:h2")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:${Versions.jwt}")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:${Versions.jwt}")
    runtimeOnly("org.liquibase:liquibase-core:${Versions.liquibase}")

    testAnnotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    "intTestAnnotationProcessor"("org.projectlombok:lombok")
    "intTestCompileOnly"("org.projectlombok:lombok")

    "intTestImplementation"("org.springframework.cloud:spring-cloud-starter-openfeign:${Versions.springFeign}")
    "intTestImplementation"("io.github.openfeign:feign-jackson:${Versions.feign}")
}

tasks {

    register<Test>("integrationTest") {
        description = "Runs the integration tests."
        group = "verification"

        testClassesDirs = sourceSets["intTest"].output.classesDirs
        classpath = sourceSets["intTest"].runtimeClasspath
        shouldRunAfter("test")
    }

    named("check") { dependsOn("integrationTest") }

}