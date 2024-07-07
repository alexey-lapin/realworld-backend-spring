import org.graalvm.buildtools.gradle.tasks.BuildNativeImageTask
import org.gradle.internal.os.OperatingSystem
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.graalvm)
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

graalvmNative {
    toolchainDetection.set(true)
    binaries {
        named("main") {
            imageName.set(rootProject.name)
            buildArgs.add("--verbose")
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
