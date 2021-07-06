package top.warmthdawn.emss.plugins

import ch.qos.logback.classic.Logger
import io.ktor.application.*
import io.ktor.features.*
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import top.warmthdawn.emss.config.AppConfig

/**
 *
 * @author WarmthDawn
 * @since 2021-07-07
 */

fun Application.configureLogging() {
    val appConfig by inject<AppConfig>()
    if (!appConfig.serverConfig.isProd) {
        val root = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME) as Logger
        root.level = ch.qos.logback.classic.Level.TRACE
    }


    install(CallLogging) {
        level = Level.INFO
    }
}