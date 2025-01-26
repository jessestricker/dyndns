package dev.jestr.dyndns.client

typealias RecordName = String

typealias ZoneName = String

typealias IPv4Address = String

typealias IPv6Address = String

sealed interface UpdateDnsRecordRequest {
    val recordName: RecordName
    val zoneName: ZoneName

    data class A(
        override val recordName: RecordName,
        override val zoneName: ZoneName,
        val content: IPv4Address,
    ) : UpdateDnsRecordRequest

    data class AAAA(
        override val recordName: RecordName,
        override val zoneName: ZoneName,
        val content: IPv6Address,
    ) : UpdateDnsRecordRequest
}
