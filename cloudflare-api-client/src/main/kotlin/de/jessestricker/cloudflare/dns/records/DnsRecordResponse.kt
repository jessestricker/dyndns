package de.jessestricker.cloudflare.dns.records

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule

@Serializable
sealed interface DnsRecordResponse {
    @SerialName("id")
    val id: String

    @SerialName("name")
    val name: String

    @SerialName("content")
    val content: String

    @Serializable
    @SerialName("A")
    data class ARecord(
        override val id: String,
        override val name: String,
        override val content: String,
    ) : DnsRecordResponse

    @Serializable
    @SerialName("AAAA")
    data class AAAARecord(
        override val id: String,
        override val name: String,
        override val content: String,
    ) : DnsRecordResponse

    @Serializable
    data class Other(
        override val id: String,
        override val name: String,
        override val content: String,
        @SerialName("type") val type: String,
    ) : DnsRecordResponse

    companion object {
        internal val serializersModule = SerializersModule {
            polymorphicDefaultDeserializer(DnsRecordResponse::class) { Other.serializer() }
        }
    }
}
