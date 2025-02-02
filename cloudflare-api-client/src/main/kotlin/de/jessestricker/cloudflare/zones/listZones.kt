package de.jessestricker.cloudflare.zones

import de.jessestricker.cloudflare.CloudflareApiClient
import de.jessestricker.cloudflare.pathSegments
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

suspend fun CloudflareApiClient.listZones(name: String? = null): ListZonesResponse {
    return httpClient
        .get {
            pathSegments("zones")
            parameter("name", name)
        }
        .body()
}

@Serializable
data class ListZonesResponse(
    @SerialName("result") val result: List<Zone>?,
)
