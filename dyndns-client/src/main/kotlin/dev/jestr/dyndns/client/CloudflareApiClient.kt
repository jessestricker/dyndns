package dev.jestr.dyndns.client

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.auth.AuthScheme
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.http.contentType
import io.ktor.http.path
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import org.slf4j.LoggerFactory

private const val BASE_URL = "https://api.cloudflare.com/client/v4/"
private const val DEFAULT_PAGE_SIZE = 5_000_000

internal class CloudflareApiClient(apiToken: String) : AutoCloseable {
    private val logger = LoggerFactory.getLogger(javaClass)

    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        ignoreUnknownKeys = true
        namingStrategy = JsonNamingStrategy.Builtins.KebabCase
        serializersModule +
            SerializersModule {
                polymorphicDefaultDeserializer(Record::class) { UnknownRecord.serializer() }
            }
    }

    private val httpClient =
        HttpClient(CIO) {
            expectSuccess = true
            install(DefaultRequest) {
                url(BASE_URL)
                header(
                    HttpHeaders.Authorization,
                    HttpAuthHeader.Single(AuthScheme.Bearer, apiToken),
                )
                contentType(ContentType.Application.Json)
            }
            install(ContentNegotiation) { json(json) }
        }

    override fun close() {
        httpClient.close()
    }

    @Serializable data class ListDnsRecordsRequest(val perPage: Int = DEFAULT_PAGE_SIZE)

    suspend fun listDnsRecords(
        zoneId: String,
        request: ListDnsRecordsRequest = ListDnsRecordsRequest(),
    ): List<Record> {
        logger
            .atDebug()
            .addKeyValue("zoneId", zoneId)
            .addKeyValue("request", request)
            .log("listDnsRecords")

        val response: List<Record> =
            httpClient
                .get {
                    url { path("zones", zoneId, "dns_records") }
                    setBody(request)
                }
                .bodyUnwrapped()

        logger.atDebug().addKeyValue("response", response).log("listDnsRecords")

        return response
    }

    @Serializable
    data class BatchDnsRecordsRequest(
        val posts: List<NewRecord> = emptyList(),
        val patches: List<Record> = emptyList(),
    )

    @Serializable
    data class BatchDnsRecordsResponse(
        val posts: List<Record> = emptyList(),
        val patches: List<Record> = emptyList(),
    )

    suspend fun batchDnsRecords(
        zoneId: String,
        request: BatchDnsRecordsRequest = BatchDnsRecordsRequest(),
    ): BatchDnsRecordsResponse {
        logger
            .atDebug()
            .addKeyValue("zoneId", zoneId)
            .addKeyValue("request", request)
            .log("batchDnsRecords")

        val response: BatchDnsRecordsResponse =
            httpClient
                .post {
                    url { path("zones", zoneId, "dns_records", "batch") }
                    setBody(request)
                }
                .bodyUnwrapped()

        logger.atDebug().addKeyValue("response", response).log("batchDnsRecords")

        return response
    }

    @Serializable sealed interface NewRecord

    @Serializable
    @SerialName("A")
    data class NewARecord(val name: String, val content: String) : NewRecord

    @Serializable
    @SerialName("AAAA")
    data class NewAAAARecord(val name: String, val content: String) : NewRecord

    @Serializable sealed interface Record

    @Serializable
    @SerialName("A")
    data class ARecord(val id: String, val name: String, val content: String) : Record

    @Serializable
    @SerialName("AAAA")
    data class AAAARecord(val id: String, val name: String, val content: String) : Record

    @Serializable data class UnknownRecord(val type: String) : Record

    @Serializable private data class ResponseWrapper<T>(val result: T)

    private suspend inline fun <reified T> HttpResponse.bodyUnwrapped(): T =
        body<ResponseWrapper<T>>().result
}
