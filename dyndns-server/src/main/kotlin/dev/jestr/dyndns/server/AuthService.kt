package dev.jestr.dyndns.server

import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.UserPasswordCredential
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory

class AuthService(private val config: Config) {
    init {
        check(config.username.isNotEmpty()) { "username must not be empty" }
        check(config.password.isNotEmpty()) { "password must not be empty" }
    }

    private val logger = LoggerFactory.getLogger(javaClass)

    fun authenticate(credential: UserPasswordCredential): UserIdPrincipal? {
        if (credential.name == config.username && credential.password == config.password) {
            return UserIdPrincipal(credential.name)
        }
        logger.atWarn().addKeyValue("username", credential.name).log("authentication failed")
        return null
    }

    @Serializable data class Config(val username: String, val password: String)
}
