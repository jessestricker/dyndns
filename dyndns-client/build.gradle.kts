plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)
}

dependencies {
    implementation(libs.ktor.client.core)
    runtimeOnly(libs.ktor.client.cio)
    implementation(libs.ktor.client.contentNegotiation)
    implementation(libs.ktor.serialization.kotlinxJson)
    implementation(libs.kotlinLogging)
}

kotlin {
    jvmToolchain {
        languageVersion = libs.versions.java.map { JavaLanguageVersion.of(it) }
    }
}
