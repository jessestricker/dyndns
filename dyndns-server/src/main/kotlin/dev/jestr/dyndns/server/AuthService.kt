package dev.jestr.dyndns.server

import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.UserPasswordCredential
import kotlinx.serialization.Serializable

class AuthService(private val config: Config) {
    init {
        check(config.username.isNotEmpty()) { "username must not be empty" }
        check(config.password.isNotEmpty()) { "password must not be empty" }
    }

    fun authenticate(credential: UserPasswordCredential): UserIdPrincipal? {
        if (credential.name == config.username && credential.password == config.password) {
            return UserIdPrincipal(credential.name)
        }
        return null
    }

    @Serializable data class Config(val username: String, val password: String)
}
