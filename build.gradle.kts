plugins {
    java
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("com.github.ben-manes.versions")
}

allprojects {
    apply(plugin="idea")

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin="java")

    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    tasks {
        withType<Test> {
            useJUnitPlatform()
        }
    }
}