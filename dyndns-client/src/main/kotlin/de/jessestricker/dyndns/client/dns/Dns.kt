package de.jessestricker.dyndns.client.dns

data class Zone(
    val id: String,
    val name: String,
)

sealed interface Record {
    val id: String
    val name: String
    val content: String
}

data class ARecord(
    override val id: String,
    override val name: String,
    override val content: String,
) : Record

data class AAAARecord(
    override val id: String,
    override val name: String,
    override val content: String,
) : Record

sealed interface NewRecord {
    val name: String
    val content: String
}

data class NewARecord(
    override val name: String,
    override val content: String,
) : NewRecord

data class NewAAAARecord(
    override val name: String,
    override val content: String,
) : NewRecord
