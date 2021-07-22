package top.warmthdawn.emss.config

import io.ktor.application.*
import org.koin.ktor.ext.inject

class AppConfig {
    lateinit var serverConfig: ServerConfig
    var testing: Boolean = false
    var useMemory: Boolean = true
    // Place here other configurations

    lateinit var secretKeyConfig: SecretKeyConfig
}

fun Application.setupConfig(testing: Boolean = false) {
    val appConfig by inject<AppConfig>()
    appConfig.testing = testing
    appConfig.useMemory = environment.config.propertyOrNull("test.useMemoryDB")?.getString()?.toBoolean() ?: true
    // Server
    val serverObject = environment.config.config("ktor.server")
    val isProd = serverObject.property("isProd").getString().toBoolean()
    appConfig.serverConfig = ServerConfig(isProd)

    // secretKey
    val secretKeyObject = environment.config.config("key")
    val authKey = secretKeyObject.property("authKey").getString()
    val passwordFrontKey = secretKeyObject.property("passwordFrontKey").getString()
    val passwordBackKey = secretKeyObject.property("passwordBackKey").getString()
    appConfig.secretKeyConfig = SecretKeyConfig(authKey,passwordFrontKey,passwordBackKey)
}

data class ServerConfig(
    val isProd: Boolean
)

data class SecretKeyConfig(
    val authKey: String,
    val passwordFrontKey: String,
    val passwordBackKey: String
)
