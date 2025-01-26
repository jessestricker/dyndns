package dev.jestr.dyndns.client

data class UpdateDnsRecordRequest(
    val zoneName: String,
    val recordName: String,
    val ipv4Address: String?,
    val ipv6Address: String?,
) {
    init {
        require(ipv4Address != null || ipv6Address != null) {
            "at least one IP address must not be null"
        }
    }
}
