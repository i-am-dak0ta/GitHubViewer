plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
    kotlin("plugin.serialization") version libs.versions.kotlin
}


dependencies {

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)

    // Javax Inject
    implementation(libs.javax.inject)

    // Retrofit
    implementation(libs.retrofit)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Tests
    testImplementation(libs.junit)
}