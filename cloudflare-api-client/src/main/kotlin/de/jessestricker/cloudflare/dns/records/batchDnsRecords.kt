package de.jessestricker.cloudflare.dns.records

import de.jessestricker.cloudflare.CloudflareApiClient
import de.jessestricker.cloudflare.pathSegments
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

suspend fun CloudflareApiClient.batchDnsRecords(zoneId: String, request: BatchDnsRecordsRequest) {
    httpClient
        .post {
            pathSegments("zones", zoneId, "dns_records", "batch")
            setBody(request)
        }
}

@Serializable
data class BatchDnsRecordsRequest(
    @SerialName("patches") val patches: List<DnsRecordBatchPatch>? = null,
    @SerialName("posts") val posts: List<DnsRecordBatchPost>? = null,
)
