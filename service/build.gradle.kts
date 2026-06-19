import org.graalvm.buildtools.gradle.tasks.BuildNativeImageTask
import org.gradle.internal.os.OperatingSystem
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage
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

    implementation(libs.instrumentation.opentelemetryLogbackAppender10)
    implementation(libs.mapstruct.core)
    implementation(libs.mapstruct.springAnnotations)
    implementation(libs.mapstruct.springExtensions)
    implementation(libs.spring.springBootStarterActuator)
    implementation(libs.spring.springBootStarterDataJdbc)
    implementation(libs.spring.springBootStarterLiquibase)
    implementation(libs.spring.springBootStarterOauth2ResourceServer)
    implementation(libs.spring.springBootStarterOpentelemetry)
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

    val writeArtifactFile by registering {
        doLast {
            val outputDirectory = getByName<BuildNativeImageTask>("nativeCompile").outputDirectory
            outputDirectory.get().asFile.mkdirs()
            outputDirectory.file("gradle-artifact.txt")
                .get().asFile
                .writeText("${rootProject.name}-${project.version}-${platform()}")
        }
    }

    named("nativeCompile") {
        finalizedBy(writeArtifactFile)
    }

}

fun platform(): String {
    val os = OperatingSystem.current()
    val arc = System.getProperty("os.arch")
    return when {
        OperatingSystem.current().isWindows -> "windows-${arc}"
        OperatingSystem.current().isLinux -> "linux-${arc}"
        OperatingSystem.current().isMacOsX -> "darwin-${arc}"
        else -> os.nativePrefix
    }
}
