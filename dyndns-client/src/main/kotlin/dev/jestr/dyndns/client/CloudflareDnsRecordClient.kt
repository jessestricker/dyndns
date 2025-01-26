package dev.jestr.dyndns.client

import dev.jestr.dyndns.client.cloudflare.CloudflareClient
import dev.jestr.dyndns.client.cloudflare.CloudflareClient.AAAARecord
import dev.jestr.dyndns.client.cloudflare.CloudflareClient.ARecord
import dev.jestr.dyndns.client.cloudflare.CloudflareClient.BatchDnsRecordsRequest
import dev.jestr.dyndns.client.cloudflare.CloudflareClient.NewAAAARecord
import dev.jestr.dyndns.client.cloudflare.CloudflareClient.NewARecord
import dev.jestr.dyndns.client.cloudflare.CloudflareClient.NewRecord
import dev.jestr.dyndns.client.cloudflare.CloudflareClient.Record
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory

class CloudflareDnsRecordClient(private val config: Config) : DnsRecordClient, AutoCloseable {
    @Serializable
    @SerialName("cloudflare")
    data class Config(val zoneId: String, val apiToken: String) : DnsRecordClient.Config

    private val logger = LoggerFactory.getLogger(javaClass)
    private val client = CloudflareClient(config.apiToken)

    override fun close() {
        client.close()
    }

    override suspend fun updateDnsRecord(request: UpdateDnsRecordRequest) {
        updateDnsRecords(listOf(request))
    }

    override suspend fun updateDnsRecords(requests: List<UpdateDnsRecordRequest>) {
        logger.atInfo().addKeyValue("requests", requests).log("update DNS records")

        val listDnsRecordsResponse = client.listDnsRecords(zoneId = config.zoneId)

        val existingRecords = listDnsRecordsResponse.result
        val recordsToCreate = mutableListOf<NewRecord>()
        val recordsToUpdate = mutableListOf<Record>()

        for (request in requests) {
            val domainName = request.recordName + "." + request.zoneName
            when (request) {
                is UpdateDnsRecordRequest.A -> {
                    val existingRecord = existingRecords.findARecordByName(domainName)
                    if (existingRecord != null) {
                        if (existingRecord.content != request.content) {
                            recordsToUpdate += existingRecord.copy(content = request.content)
                        }
                    } else {
                        recordsToCreate +=
                            NewARecord(name = request.recordName, content = request.content)
                    }
                }

                is UpdateDnsRecordRequest.AAAA -> {
                    val existingRecord = existingRecords.findAAAARecordByName(domainName)
                    if (existingRecord != null) {
                        if (existingRecord.content != request.content) {
                            recordsToUpdate += existingRecord.copy(content = request.content)
                        }
                    } else {
                        recordsToCreate +=
                            NewAAAARecord(name = request.recordName, content = request.content)
                    }
                }
            }
        }

        if (recordsToCreate.isEmpty() && recordsToUpdate.isEmpty()) {
            logger.atDebug().log("already up-to-date")
            return
        }

        client.batchDnsRecords(
            zoneId = config.zoneId,
            request = BatchDnsRecordsRequest(posts = recordsToCreate, patches = recordsToUpdate),
        )
    }

    private fun List<Record>.findARecordByName(name: String) =
        asSequence().filterIsInstance<ARecord>().find { it.name == name }

    private fun List<Record>.findAAAARecordByName(name: String) =
        asSequence().filterIsInstance<AAAARecord>().find { it.name == name }
}
