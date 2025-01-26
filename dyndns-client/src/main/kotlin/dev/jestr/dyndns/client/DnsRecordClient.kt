package dev.jestr.dyndns.client

import kotlinx.serialization.Serializable

sealed interface DnsRecordClient {
    @Serializable sealed interface Config

    suspend fun updateDnsRecord(request: UpdateDnsRecordRequest)

    suspend fun updateDnsRecords(requests: List<UpdateDnsRecordRequest>)
}
