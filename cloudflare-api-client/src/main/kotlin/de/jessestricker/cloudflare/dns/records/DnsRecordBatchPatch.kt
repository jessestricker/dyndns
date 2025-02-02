package de.jessestricker.cloudflare.dns.records

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface DnsRecordBatchPatch {
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
    ) : DnsRecordBatchPatch

    @Serializable
    @SerialName("AAAA")
    data class AAAARecord(
        override val id: String,
        override val name: String,
        override val content: String,
    ) : DnsRecordBatchPatch
}
