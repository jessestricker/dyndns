plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)
    application
}

version = "0.2.0"

repositories { //
    mavenCentral()
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.callId)
    implementation(libs.ktor.server.callLogging)
    implementation(libs.kaml)
    runtimeOnly(libs.logback.classic)

    implementation(project(":dyndns-client"))
}

kotlin { //
    jvmToolchain { //
        languageVersion = libs.versions.java.map { JavaLanguageVersion.of(it) }
    }
}

application { //
    mainClass = "dev.jestr.dyndns.server.ApplicationKt"
}

distributions.main {
    contents {
        from("contrib") {
            into("etc")
            expand("version" to project.version)
        }
    }
}

tasks.named<JavaExec>("run") { //
    environment("DYNDNS_CONFIG", "develop/config.yml")
    jvmArgs("-Dlogback.configurationFile=develop/logback.xml")
}
