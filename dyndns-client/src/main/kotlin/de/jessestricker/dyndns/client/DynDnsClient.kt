package de.jessestricker.dyndns.client

import de.jessestricker.dyndns.client.dns.*
import io.github.oshai.kotlinlogging.KotlinLogging

class DynDnsClient(private val dnsClient: DnsClient) {
    private val logger = KotlinLogging.logger {}

    suspend fun update(
        zoneName: String,
        domainName: String,
        ipv4Addresses: Set<String>,
        ipv6Addresses: Set<String>,
    ) {
        logger.info { "update domain '$domainName' to IPv4 $ipv4Addresses and IPv6 $ipv6Addresses" }

        // get current records with given domain name
        val zone = dnsClient.getZone(zoneName)
        val records = dnsClient.getRecords(zone, domainName)

        // find outdated records
        val outdatedRecords =
            records.filter { record ->
                when (record) {
                    is ARecord -> record.content !in ipv4Addresses
                    is AAAARecord -> record.content !in ipv6Addresses
                }
            }

        // find missing records
        val missingARecords =
            ipv4Addresses
                .filter { ipv4Address ->
                    records.none { it is ARecord && it.content == ipv4Address }
                }
                .map { NewARecord(name = domainName, content = it) }
        val missingAAAARecords =
            ipv6Addresses
                .filter { ipv6Address ->
                    records.none { it is AAAARecord && it.content == ipv6Address }
                }
                .map { NewAAAARecord(name = domainName, content = it) }
        val missingRecords = missingARecords + missingAAAARecords

        // delete outdated records and create missing records
        if (outdatedRecords.isEmpty() && missingRecords.isEmpty()) {
            logger.info { "already up-to-date" }
            return
        }

        logger.info { "deleting $outdatedRecords and creating $missingRecords" }
        dnsClient.updateRecords(zone = zone, toDelete = outdatedRecords, toCreate = missingRecords)
    }
}
