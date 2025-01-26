package dev.jestr.dyndns.client

import kotlinx.serialization.Serializable

sealed interface DynDnsClient {
    @Serializable sealed interface Config

    suspend fun update(request: UpdateDynDnsRequest)

    companion object {
        fun fromConfig(config: Config): DynDnsClient {
            return when (config) {
                is CloudflareDynDnsClient.Config -> CloudflareDynDnsClient(config)
            }
        }
    }
}
