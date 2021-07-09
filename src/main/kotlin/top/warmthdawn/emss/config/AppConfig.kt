package top.warmthdawn.emss.config

import io.ktor.application.*
import org.koin.ktor.ext.inject

class AppConfig {
    lateinit var serverConfig: ServerConfig
    var testing: Boolean = false
    // Place here other configurations
}

fun Application.setupConfig(testing: Boolean = false) {
    val appConfig by inject<AppConfig>()
    appConfig.testing = testing
    // Server
    val serverObject = environment.config.config("ktor.server")
    val isProd = serverObject.property("isProd").getString().toBoolean()
    appConfig.serverConfig = ServerConfig(isProd)

}

data class ServerConfig(
    val isProd: Boolean
)
