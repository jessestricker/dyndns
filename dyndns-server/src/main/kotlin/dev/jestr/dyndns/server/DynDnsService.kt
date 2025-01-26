package dev.jestr.dyndns.server

import dev.jestr.dyndns.client.CloudflareDnsRecordClient
import dev.jestr.dyndns.client.DnsRecordClient
import dev.jestr.dyndns.client.UpdateDnsRecordRequest
import dev.jestr.dyndns.client.ZoneName
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.NotFoundException
import org.slf4j.LoggerFactory

typealias DynDnsServiceConfig = Map<ZoneName, DnsRecordClient.Config>

class DynDnsService(config: DynDnsServiceConfig) {
    init {
        require(config.isNotEmpty()) { "zones must not be empty" }
        require(config.keys.all { it.isNotEmpty() }) { "zone names must not be empty" }
    }

    private val logger = LoggerFactory.getLogger(javaClass)

    private val dnsRecordClients: Map<String, DnsRecordClient> =
        config.mapValues { (_, dynDnsClientConfig) ->
            when (dynDnsClientConfig) {
                is CloudflareDnsRecordClient.Config -> CloudflareDnsRecordClient(dynDnsClientConfig)
            }
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

        val updateRequests =
            listOfNotNull(
                ipv4Addr
                    ?.takeUnless { it.isEmpty() }
                    ?.let {
                        UpdateDnsRecordRequest.A(
                            recordName = recordName,
                            zoneName = zoneName,
                            content = it,
                        )
                    },
                ipv6Addr
                    ?.takeUnless { it.isEmpty() }
                    ?.let {
                        UpdateDnsRecordRequest.AAAA(
                            recordName = recordName,
                            zoneName = zoneName,
                            content = it,
                        )
                    },
            )
        if (updateRequests.isEmpty()) {
            throw BadRequestException("At least one non-empty IP addresses must be given.")
        }

        dynDnsClient.updateDnsRecords(updateRequests)
    }

    private fun parseDomainName(domainName: String): ParsedDomainName? {
        for ((zoneName, dynDnsClient) in dnsRecordClients) {
            val domainNameSuffix = ".$zoneName"
            if (domainName.endsWith(domainNameSuffix)) {
                val recordName = domainName.removeSuffix(domainNameSuffix)
                return ParsedDomainName(
                    zoneName = zoneName,
                    recordName = recordName,
                    dnsRecordClient = dynDnsClient,
                )
            }
        }
        return null
    }

    private data class ParsedDomainName(
        val zoneName: String,
        val recordName: String,
        val dnsRecordClient: DnsRecordClient,
    )
}
