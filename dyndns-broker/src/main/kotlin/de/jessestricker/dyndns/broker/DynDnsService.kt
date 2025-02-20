package de.jessestricker.dyndns.broker

import de.jessestricker.dyndns.client.DynDnsClient
import de.jessestricker.dyndns.client.dns.DnsClient

typealias DynDnsServiceConfig = Map<String, DnsClient.Config>

class DynDnsService(
    private val dynDnsClients: Map<String, DynDnsClient>,
) {
    suspend fun update(
        zoneName: String,
        domainName: String,
        ipv4Addresses: Set<String>,
        ipv6Addresses: Set<String>,
    ) {
        val dynDnsClient = dynDnsClients[zoneName] ?: throw UnknownZoneException(zoneName)
        dynDnsClient.update(zoneName, domainName, ipv4Addresses, ipv6Addresses)
    }

    companion object {
        fun fromConfig(zones: DynDnsServiceConfig): DynDnsService {
            val dynDnsClients = zones.mapValues { (_, config) ->
                val dnsClient = DnsClient.fromConfig(config)
                DynDnsClient(dnsClient)
            }
            return DynDnsService(dynDnsClients)
        }
    }

    class UnknownZoneException(zoneName: String) : RuntimeException("Unknown zone '$zoneName'")
}
