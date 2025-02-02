package de.jessestricker.cloudflare.dns.records

import de.jessestricker.cloudflare.CloudflareApiClient
import de.jessestricker.cloudflare.pathSegments
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

suspend fun CloudflareApiClient.listDnsRecords(zoneId: String, name: String? = null): ListDnsRecordsResponse {
    return httpClient
        .get {
            pathSegments("zones", zoneId, "dns_records")
            parameter("name", name)
        }
        .body()
}

@Serializable
data class ListDnsRecordsResponse(
    @SerialName("result") val result: List<DnsRecordResponse>?,
)
