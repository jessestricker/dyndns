package dev.jestr.dyndns.client

import dev.jestr.dyndns.client.CloudflareApiClient.AAAARecord
import dev.jestr.dyndns.client.CloudflareApiClient.ARecord
import dev.jestr.dyndns.client.CloudflareApiClient.BatchDnsRecordsRequest
import dev.jestr.dyndns.client.CloudflareApiClient.NewAAAARecord
import dev.jestr.dyndns.client.CloudflareApiClient.NewARecord
import dev.jestr.dyndns.client.CloudflareApiClient.NewRecord
import dev.jestr.dyndns.client.CloudflareApiClient.Record
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory

class CloudflareDynDnsClient(private val config: Config) : DynDnsClient, AutoCloseable {
    @Serializable
    @SerialName("cloudflare")
    data class Config(val zoneId: String, val apiToken: String) : DynDnsClient.Config

    private val logger = LoggerFactory.getLogger(javaClass)
    private val apiClient = CloudflareApiClient(config.apiToken)

    override fun close() {
        apiClient.close()
    }

    override suspend fun update(request: UpdateDynDnsRequest) {
        val domainName = request.recordName + "." + request.zoneName
        logger.info(
            "domain '{}': update IPv4 to '{}' and IPv6 to '{}'",
            domainName,
            request.ipv4Address,
            request.ipv6Address,
        )

        val existingRecords = apiClient.listDnsRecords(config.zoneId)

        val recordsToCreate = mutableListOf<NewRecord>()
        val recordsToUpdate = mutableListOf<Record>()

        if (request.ipv4Address != null) {
            val existingRecord = existingRecords.findARecordByName(domainName)
            if (existingRecord == null) {
                // no existing A record, create a new one
                recordsToCreate += NewARecord(request.recordName, request.ipv4Address)
            } else if (existingRecord.content != request.ipv4Address) {
                // existing A record but with different IPv4 address, update existing
                recordsToUpdate += existingRecord.copy(content = request.ipv4Address)
            }
        }

        if (request.ipv6Address != null) {
            val existingRecord = existingRecords.findAAAARecordByName(domainName)
            if (existingRecord == null) {
                // no existing AAAA record, create a new one
                recordsToCreate += NewAAAARecord(request.recordName, request.ipv6Address)
            } else if (existingRecord.content != request.ipv6Address) {
                // existing AAAA record but with different IPv6 address, update existing
                recordsToUpdate += existingRecord.copy(content = request.ipv6Address)
            }
        }

        if (recordsToCreate.isEmpty() && recordsToUpdate.isEmpty()) {
            logger.info("domain '{}': already up-to-date", domainName)
            return
        } else {
            logger.info(
                "domain '{}': creating {} and updating {}",
                domainName,
                recordsToCreate,
                recordsToUpdate,
            )
        }

        apiClient.batchDnsRecords(
            config.zoneId,
            BatchDnsRecordsRequest(recordsToCreate, recordsToUpdate),
        )
    }

    private fun List<Record>.findARecordByName(name: String) =
        asSequence().filterIsInstance<ARecord>().find { it.name == name }

    private fun List<Record>.findAAAARecordByName(name: String) =
        asSequence().filterIsInstance<AAAARecord>().find { it.name == name }
}
