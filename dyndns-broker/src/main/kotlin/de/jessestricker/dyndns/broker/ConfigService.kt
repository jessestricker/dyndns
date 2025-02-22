package de.jessestricker.dyndns.broker

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import java.nio.file.Path
import kotlin.io.path.inputStream
import kotlinx.serialization.Serializable

class ConfigService(private val configFilePath: Path) {
    val config: Config by lazy {
        configFilePath.inputStream().buffered().use { stream ->
            Yaml.default.decodeFromStream(stream)
        }
    }

    @Serializable
    data class Config(val server: Server, val dyndns: DynDnsServiceConfig) {
        @Serializable data class Server(val port: Int)
    }
}
