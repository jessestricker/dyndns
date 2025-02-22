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

private const val CALL_ID_LENGTH = 16
private const val CALL_ID_DICTIONARY = "abcdefghijklmnopqrstuvwxyz0123456789"

fun main() {
    val configFilePath = Path(environmentVariable("DYNDNS_BROKER_CONFIG"))
    val configService = ConfigService(configFilePath)
    val config = configService.config

    val dynDnsService = DynDnsService.fromConfig(config.dyndns)

    val server =
        embeddedServer(CIO, port = config.server.port) {
            install(CallId) { generate(CALL_ID_LENGTH, CALL_ID_DICTIONARY) }
            install(CallLogging) { callIdMdc("callId") }
            setupRouting(dynDnsService)
        }
    server.start(wait = true)
}

private fun Application.setupRouting(dynDnsService: DynDnsService) {
    routing {
        get("/update/{zoneName}/{recordName}") {
            val zoneName = call.pathParameters.getOrFail("zoneName")
            val recordName = call.pathParameters.getOrFail("recordName")
            val ipv4Addresses = call.queryParameters.getAll("ipv4").filterIpAddresses()
            val ipv6Addresses = call.queryParameters.getAll("ipv6").filterIpAddresses()

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

private fun List<String>?.filterIpAddresses(): Set<String> =
    orEmpty().filter { it.isNotBlank() }.toSet()

private fun environmentVariable(name: String): String =
    System.getenv(name) ?: error("""environment variable "$name" undefined""")
