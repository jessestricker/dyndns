plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.spotless)
    application
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.callId)
    implementation(libs.ktor.server.callLogging)
    implementation(libs.kaml)
    runtimeOnly(libs.logback.classic)
    implementation(project(":dyndns-client"))
}

kotlin {
    jvmToolchain {
        languageVersion = libs.versions.java.map { JavaLanguageVersion.of(it) }
    }
}

application {
    mainClass = "de.jessestricker.dyndns.broker.ApplicationKt"
}

distributions.main {
    contents {
        into("etc") {
            from("etc/config.yml")
            from("etc/dyndns-broker.service")
        }
    }
}

spotless {
    kotlin {
        ktfmt(libs.versions.ktfmt.get()).kotlinlangStyle()
    }
}
