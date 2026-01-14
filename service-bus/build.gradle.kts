plugins {
    alias(libs.plugins.spring.nullability)
    id("realworld.java-conventions")
}

dependencies {
    annotationProcessor(libs.projectlombok.lombok)
    compileOnly(libs.projectlombok.lombok)

    implementation(libs.spring.springContext)
}
