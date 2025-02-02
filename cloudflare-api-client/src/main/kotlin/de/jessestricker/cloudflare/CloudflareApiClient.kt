package de.jessestricker.cloudflare

import de.jessestricker.cloudflare.dns.records.DnsRecordResponse
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.http.appendPathSegments
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.plus

private const val BASE_URL = "https://api.cloudflare.com/client/v4/"

class CloudflareApiClient(private val apiToken: String) {
    internal val httpClient = HttpClient(CIO) {
        expectSuccess = true
        install(DefaultRequest) {
            url(BASE_URL)
            bearerAuth(apiToken)
        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                serializersModule += DnsRecordResponse.serializersModule
            })
        }
    }
}

internal fun HttpRequestBuilder.pathSegments(vararg segments: String) {
    url.appendPathSegments(*segments, encodeSlash = true)
}
