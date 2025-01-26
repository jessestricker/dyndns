package dev.jestr.dyndns.server

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.basic
import io.ktor.server.cio.CIO
import io.ktor.server.engine.addShutdownHook
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.plugins.callid.CallId
import io.ktor.server.plugins.callid.callIdMdc
import io.ktor.server.plugins.callid.generate
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import org.slf4j.event.Level
import kotlin.io.path.Path

private const val ENV_VAR_CONFIG = "DYNDNS_CONFIG"
private const val CALL_ID_DICTIONARY = "0123456789abcdef"
private const val CALL_ID_LENGTH = 16

fun main() {
    val configFilePath = System.getenv(ENV_VAR_CONFIG)
    checkNotNull(configFilePath) { "Environment variable '$ENV_VAR_CONFIG' not set." }
    val configService = ConfigService(Path(configFilePath))
    val config = configService.config

    val authService = AuthService(config.auth)
    val dynDnsService = DynDnsService(config.zones)

    embeddedServer(CIO, port = config.port, module = { module(authService, dynDnsService) })
        .apply { addShutdownHook { dynDnsService.close() } }
        .start(wait = true)
}

fun Application.module(authService: AuthService, dynDnsService: DynDnsService) {
    install(CallLogging) {
        level = Level.DEBUG
        callIdMdc()
    }
    install(CallId) {
        verify(CALL_ID_DICTIONARY)
        generate(CALL_ID_LENGTH, CALL_ID_DICTIONARY)
    }

    install(Authentication) {
        basic("simple") { validate { credential -> authService.authenticate(credential) } }
    }

    routing {
        authenticate("simple") {
            get("/update/{domainName}") {
                val domainName = call.pathParameters["domainName"]!!
                val ipv4Addr = call.queryParameters["ipv4Addr"]
                val ipv6Addr = call.queryParameters["ipv6Addr"]

                try {
                    dynDnsService.update(domainName, ipv4Addr, ipv6Addr)
                    call.respondText("OK")
                } catch (e: BadRequestException) {
                    call.respondText(e.message.toString(), status = HttpStatusCode.BadRequest)
                } catch (e: NotFoundException) {
                    call.respondText(e.message.toString(), status = HttpStatusCode.NotFound)
                }
            }
        }
    }
}
