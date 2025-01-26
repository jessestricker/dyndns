package dev.jestr.dyndns.server

import dev.jestr.dyndns.client.DynDnsClient
import dev.jestr.dyndns.client.UpdateDynDnsRequest
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

    suspend fun update(domainName: String, ipv4Addr: String?, ipv6Addr: String?) {
        logger
            .atDebug()
            .addKeyValue("domainName", domainName)
            .addKeyValue("ipv4Addr", ipv4Addr)
            .addKeyValue("ipv6Addr", ipv6Addr)
            .log("update")

        val parsedDomainName = parseDomainName(domainName)
        if (parsedDomainName == null) {
            throw NotFoundException("Unknown domain name.")
        }
        val (zoneName, recordName, dynDnsClient) = parsedDomainName

        val request =
            UpdateDynDnsRequest(
                zoneName = zoneName,
                recordName = recordName,
                ipv4Address = ipv4Addr?.takeUnless { it.isEmpty() },
                ipv6Address = ipv6Addr?.takeUnless { it.isEmpty() },
            )
        dynDnsClient.update(request)
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
