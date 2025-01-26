package dev.jestr.dyndns.server

import dev.jestr.dyndns.client.DynDnsClient
import dev.jestr.dyndns.client.UpdateDynDnsRequest
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.NotFoundException
import org.slf4j.LoggerFactory

typealias DynDnsServiceConfig = Map<String, DynDnsClient.Config>

class DynDnsService(config: DynDnsServiceConfig) : AutoCloseable {
    init {
        require(config.isNotEmpty()) { "zones must not be empty" }
        require(config.keys.all { it.isNotEmpty() }) { "zone names must not be empty" }
    }

    private val logger = LoggerFactory.getLogger(javaClass)
    private val dynDnsClients: Map<String, DynDnsClient> =
        config.mapValues { (_, clientConfig) -> DynDnsClient.fromConfig(clientConfig) }

    override fun close() {
        dynDnsClients.asSequence().filterIsInstance<AutoCloseable>().forEach { it.close() }
    }

    suspend fun update(domainName: String, ipv4Address: String?, ipv6Address: String?) {
        logger
            .atDebug()
            .addKeyValue("domainName", domainName)
            .addKeyValue("ipv4Address", ipv4Address)
            .addKeyValue("ipv6Address", ipv6Address)
            .log("update")

        val ipv4Address = ipv4Address?.takeUnless { it.isEmpty() }
        val ipv6Address = ipv6Address?.takeUnless { it.isEmpty() }
        if (ipv4Address == null && ipv6Address == null) {
            throw BadRequestException("No IP address given.")
        }

        val parsedDomainName = parseDomainName(domainName)
        if (parsedDomainName == null) {
            throw NotFoundException("Unknown domain name.")
        }
        val (zoneName, recordName, dynDnsClient) = parsedDomainName

        dynDnsClient.update(
            UpdateDynDnsRequest(
                zoneName = zoneName,
                recordName = recordName,
                ipv4Address = ipv4Address,
                ipv6Address = ipv6Address,
            )
        )
    }

    private fun parseDomainName(domainName: String): ParsedDomainName? {
        for ((zoneName, dynDnsClient) in dynDnsClients) {
            val domainNameSuffix = ".$zoneName"
            if (domainName.endsWith(domainNameSuffix)) {
                val recordName = domainName.removeSuffix(domainNameSuffix)
                return ParsedDomainName(
                    zoneName = zoneName,
                    recordName = recordName,
                    dynDnsClient = dynDnsClient,
                )
            }
        }
        return null
    }

    private data class ParsedDomainName(
        val zoneName: String,
        val recordName: String,
        val dynDnsClient: DynDnsClient,
    )
}
