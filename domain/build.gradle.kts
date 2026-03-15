plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    // Kotlin
    implementation(libs.kotlinx.coroutines.core)

    // Dependency Injection
    implementation(libs.javax.inject)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.truth)
}
