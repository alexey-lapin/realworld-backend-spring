plugins {
    id("realworld.java-conventions")
}

dependencies {
    annotationProcessor(libs.projectlombok.lombok)
    compileOnly(libs.projectlombok.lombok)

    implementation(libs.spring.springContext)
}
