package dev.jestr.dyndns.server

import com.charleskorn.kaml.PolymorphismStyle
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.charleskorn.kaml.decodeFromStream
import kotlinx.serialization.Serializable
import java.nio.file.Path
import kotlin.io.path.inputStream

class ConfigService(private val filePath: Path) {
    @Serializable
    data class Config(val port: Int, val auth: AuthService.Config, val zones: DynDnsServiceConfig)

    private val yaml =
        Yaml(configuration = YamlConfiguration(polymorphismStyle = PolymorphismStyle.Property))

    val config by lazy { loadConfig() }

    private fun loadConfig(): Config {
        return filePath.inputStream().use { inputStream -> yaml.decodeFromStream(inputStream) }
    }
}
