package de.jessestricker.cloudflare.dns.records

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface DnsRecordBatchPost {
    @SerialName("name")
    val name: String

    @SerialName("content")
    val content: String

    @Serializable
    @SerialName("A")
    data class ARecord(
        override val name: String,
        override val content: String,
    ) : DnsRecordBatchPost

    @Serializable
    @SerialName("AAAA")
    data class AAAARecord(
        override val name: String,
        override val content: String,
    ) : DnsRecordBatchPost
}
