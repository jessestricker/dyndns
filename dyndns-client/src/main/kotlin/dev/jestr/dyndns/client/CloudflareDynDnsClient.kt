package dev.jestr.dyndns.client

import dev.jestr.dyndns.client.CloudflareClient.AAAARecord
import dev.jestr.dyndns.client.CloudflareClient.ARecord
import dev.jestr.dyndns.client.CloudflareClient.BatchDnsRecordsRequest
import dev.jestr.dyndns.client.CloudflareClient.NewAAAARecord
import dev.jestr.dyndns.client.CloudflareClient.NewARecord
import dev.jestr.dyndns.client.CloudflareClient.NewRecord
import dev.jestr.dyndns.client.CloudflareClient.Record
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory

class CloudflareDynDnsClient(private val config: Config) : DynDnsClient, AutoCloseable {
    @Serializable
    @SerialName("cloudflare")
    data class Config(val zoneId: String, val apiToken: String) : DynDnsClient.Config

    private val logger = LoggerFactory.getLogger(javaClass)
    private val apiClient = CloudflareClient(config.apiToken)

    override fun close() {
        apiClient.close()
    }

    override suspend fun update(request: UpdateDnsRecordRequest) {
        logger.atInfo().addKeyValue("request", request).log("update")

        val existingRecords = apiClient.listDnsRecords(config.zoneId)

        val recordsToCreate = mutableListOf<NewRecord>()
        val recordsToUpdate = mutableListOf<Record>()
        val domainName = request.recordName + "." + request.zoneName

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
            logger.atInfo().log("records up-to-date")
            return
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
