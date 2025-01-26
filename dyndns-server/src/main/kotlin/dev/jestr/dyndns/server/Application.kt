package dev.jestr.dyndns.server

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.basic
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlin.io.path.Path

private const val ENV_VAR_CONFIG = "DYNDNS_CONFIG"

fun main() {
    val configFilePath = System.getenv(ENV_VAR_CONFIG)
    checkNotNull(configFilePath) { "Environment variable '$ENV_VAR_CONFIG' not set." }
    val configService = ConfigService(Path(configFilePath))
    val config = configService.config

    embeddedServer(CIO, port = config.port, module = { module(config) }).start(wait = true)
}

fun Application.module(config: ConfigService.Config) {
    val authService = AuthService(config.auth)
    val dynDnsService = DynDnsService(config.zones)

    install(Authentication) {
        basic("simple") { validate { credential -> authService.authenticate(credential) } }
    }

    routing {
        authenticate("simple") {
            get("/update/{domainName}") {
                val domainName = call.pathParameters["domainName"]!!
                val ipv4Addr = call.queryParameters["ipv4Addr"]
                val ipv6Addr = call.queryParameters["ipv6Addr"]
                dynDnsService.update(domainName, ipv4Addr, ipv6Addr)

                call.response.status(HttpStatusCode.OK)
            }
        }
    }
}
