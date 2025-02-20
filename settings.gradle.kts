rootProject.name = "dyndns"

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
    }
}

include(
    "dyndns-broker",
    "dyndns-client",
)
