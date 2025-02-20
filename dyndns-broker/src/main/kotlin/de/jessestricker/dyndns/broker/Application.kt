package de.jessestricker.dyndns.broker

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kotlin.io.path.Path

fun main() {
    val configFilePath = Path(environmentVariable("DYNDNS_BROKER_CONFIG"))
    val configService = ConfigService(configFilePath)
    val dynDnsService = DynDnsService.fromConfig(configService.config.dyndns)

    val server = embeddedServer(CIO, port = configService.config.server.port) {
        module(dynDnsService)
    }
    server.start(wait = true)
}

private fun Application.module(dynDnsService: DynDnsService) {
    install(CallId) {
        generate(16, "abcdefghijklmnopqrstuvwxyz0123456789")
    }
    install(CallLogging) {
        callIdMdc("callId")
    }

    routing {
        get("/update/{zoneName}/{recordName}") {
            val zoneName = call.pathParameters.getOrFail("zoneName")
            val recordName = call.pathParameters.getOrFail("recordName")
            val ipv4Addresses = call.queryParameters.getAll("ipv4").orEmpty().filter { it.isNotBlank() }.toSet()
            val ipv6Addresses = call.queryParameters.getAll("ipv6").orEmpty().filter { it.isNotBlank() }.toSet()

            val domainName = "$recordName.$zoneName"

            try {
                dynDnsService.update(zoneName, domainName, ipv4Addresses, ipv6Addresses)
            } catch (e: DynDnsService.UnknownZoneException) {
                call.respondText("${e.message}\n", status = HttpStatusCode.NotFound)
                return@get
            }

            call.respondText("OK", status = HttpStatusCode.OK)
        }
    }
}

private fun environmentVariable(name: String): String {
    return System.getenv(name) ?: error("""environment variable "$name" undefined""")
}
