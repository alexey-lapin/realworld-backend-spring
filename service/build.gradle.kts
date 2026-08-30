import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.nullability)
    alias(libs.plugins.graalvm)
    id("realworld.java-conventions")
}

dependencies {
    annotationProcessor(libs.projectlombok.lombok)
    annotationProcessor(libs.mapstruct.springAnnotations)
    annotationProcessor(libs.mapstruct.springExtensions)
    annotationProcessor(libs.mapstruct.processor)
    compileOnly(libs.projectlombok.lombok)

    implementation(project(":service-bus"))
    implementation(project(":service-api"))

    implementation(libs.mapstruct.core)
    implementation(libs.mapstruct.springAnnotations)
    implementation(libs.mapstruct.springExtensions)
    implementation(libs.spring.springBootStarterActuator)
    implementation(libs.spring.springBootStarterDataJdbc)
    implementation(libs.spring.springBootStarterLiquibase)
    implementation(libs.spring.springBootStarterOauth2ResourceServer)
    implementation(libs.spring.springBootStarterSecurity)
    implementation(libs.spring.springBootStarterValidation)
    implementation(libs.spring.springBootStarterWebmvc)
    implementation(libs.springdoc.openapiStarterWebmvcUi)

    runtimeOnly(libs.h2.h2)
    runtimeOnly(libs.spring.springBootH2console)

    testAnnotationProcessor(libs.projectlombok.lombok)
    testCompileOnly(libs.projectlombok.lombok)
    testImplementation(libs.spring.springBootStarterTest)

    intTestAnnotationProcessor(libs.projectlombok.lombok)
    intTestCompileOnly(libs.projectlombok.lombok)
    intTestImplementation(libs.spring.springBootStarterRestclientTest)
}

graalvmNative {
    toolchainDetection.set(true)
    binaries {
        named("main") {
            imageName.set(rootProject.name)
            buildArgs.add("--verbose")
            buildArgs.add("-H:DeadlockWatchdogInterval=120")
        }
    }
}

springBoot {
    buildInfo {
        properties {
            artifact = rootProject.name
        }
    }
}

tasks {
    named<BootJar>("bootJar") {
        archiveFileName.set("${rootProject.name}-${archiveVersion.get()}.${archiveExtension.get()}")
    }
}
