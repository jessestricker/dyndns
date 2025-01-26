package dev.jestr.dyndns.client

import kotlinx.serialization.Serializable

sealed interface DynDnsClient {
    @Serializable sealed interface Config

    suspend fun update(request: UpdateDnsRecordRequest)
}

fun DynDnsClient(config: DynDnsClient.Config): DynDnsClient {
    return when (config) {
        is CloudflareDynDnsClient.Config -> CloudflareDynDnsClient(config)
    }
}
