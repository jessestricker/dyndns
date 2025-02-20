package de.jessestricker.dyndns.client.dns

import kotlinx.serialization.Serializable

interface DnsClient {
    @Serializable
    sealed interface Config

    companion object {
        fun fromConfig(config: Config): DnsClient =
            when (config) {
                is CloudflareDnsClient.Config -> CloudflareDnsClient.fromConfig(config)
            }
    }

    suspend fun getZone(name: String): Zone
    suspend fun getRecords(zone: Zone, name: String): List<Record>
    suspend fun updateRecords(zone: Zone, toDelete: List<Record>, toCreate: List<NewRecord>)
}
