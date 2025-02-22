package de.jessestricker.dyndns.client.dns

import de.jessestricker.dyndns.client.internal.pathSegments
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class CloudflareDnsClient(private val apiToken: String) : DnsClient {
    @Serializable
    @SerialName("cloudflare")
    data class Config(val apiToken: String) : DnsClient.Config

    companion object {
        fun fromConfig(config: Config): CloudflareDnsClient =
            CloudflareDnsClient(apiToken = config.apiToken)
    }

    private val httpClient = HttpClient {
        expectSuccess = true
        install(DefaultRequest) {
            url("https://api.cloudflare.com/client/v4/")
            bearerAuth(apiToken)
            contentType(ContentType.Application.Json)
        }
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

    override suspend fun getZone(name: String): Zone =
        httpClient
            .get {
                pathSegments("zones")
                parameter("name", name)
            }
            .body<CfListResponse<CfZone>>()
            .result
            .single()
            .toZone()

    override suspend fun getRecords(zone: Zone, name: String): List<Record> =
        httpClient
            .get {
                pathSegments("zones", zone.id, "dns_records")
                parameter("name", name)
            }
            .body<CfListResponse<CfRecord>>()
            .result
            .mapNotNull { it.toRecordOrNull() }

    override suspend fun updateRecords(
        zone: Zone,
        toDelete: List<Record>,
        toCreate: List<NewRecord>,
    ) {
        val request =
            CfBatchRecordsRequest(
                deletes = toDelete.map { it.toCfBatchRecordsDelete() },
                posts = toCreate.map { it.toCfBatchRecordsPost() },
            )
        httpClient.post {
            pathSegments("zones", zone.id, "dns_records", "batch")
            setBody(request)
        }
    }

    @Serializable private data class CfListResponse<T>(val result: List<T>)

    @Serializable private data class CfZone(val id: String, val name: String)

    private fun CfZone.toZone(): Zone = Zone(id = id, name = name)

    @Serializable
    private data class CfRecord(
        val id: String,
        val type: String,
        val name: String,
        val content: String,
    )

    private fun CfRecord.toRecordOrNull(): Record? =
        when (type) {
            "A" -> ARecord(id = id, name = name, content = content)
            "AAAA" -> AAAARecord(id = id, name = name, content = content)
            else -> null
        }

    @Serializable
    private data class CfBatchRecordsRequest(
        val deletes: List<CfBatchRecordsDelete>,
        val posts: List<CfBatchRecordsPost>,
    )

    @Serializable private data class CfBatchRecordsDelete(val id: String)

    private fun Record.toCfBatchRecordsDelete(): CfBatchRecordsDelete =
        CfBatchRecordsDelete(id = id)

    @Serializable
    private data class CfBatchRecordsPost(val type: String, val name: String, val content: String)

    private fun NewRecord.toCfBatchRecordsPost(): CfBatchRecordsPost =
        CfBatchRecordsPost(type = type, name = name, content = content)
}

private val NewRecord.type: String
    get() =
        when (this) {
            is NewARecord -> "A"
            is NewAAAARecord -> "AAAA"
        }
