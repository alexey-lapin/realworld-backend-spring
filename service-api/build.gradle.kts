plugins {
    id("realworld.java-conventions")
}

dependencies {
    annotationProcessor(libs.projectlombok.lombok)
    compileOnly(libs.projectlombok.lombok)

    implementation(project(":service-bus"))

    implementation(libs.jackson.jacksonAnnotations)
    implementation(libs.jakarta.jakartaValidationApi)
    implementation(libs.spring.springWeb)
}
