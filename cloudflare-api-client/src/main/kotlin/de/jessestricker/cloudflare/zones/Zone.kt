package de.jessestricker.cloudflare.zones

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Zone(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("status") val status: Status?,
) {
    @Serializable
    enum class Status {
        @SerialName("initializing")
        INITIALIZING,

        @SerialName("pending")
        PENDING,

        @SerialName("active")
        ACTIVE,

        @SerialName("moved")
        MOVED,
    }
}
