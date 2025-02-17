plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)
}

version = rootProject.version

repositories { //
    mavenCentral()
}

dependencies {
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.contentNegotiation)
    implementation(libs.ktor.serialization.kotlinxJson)
}

kotlin { //
    jvmToolchain { //
        languageVersion = libs.versions.java.map { JavaLanguageVersion.of(it) }
    }
}
