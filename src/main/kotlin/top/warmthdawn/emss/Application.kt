package top.warmthdawn.emss

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.server.engine.*
import io.ktor.server.tomcat.*
import org.slf4j.LoggerFactory
import top.warmthdawn.emss.plugins.*
import ch.qos.logback.classic.Logger
import top.warmthdawn.emss.config.AppConfig
import top.warmthdawn.emss.config.setupConfig
import top.warmthdawn.emss.di.appModule
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.inject
import org.koin.logger.slf4jLogger
import org.koin.core.module.Module
import org.slf4j.event.Level

fun main(args: Array<String>) = EngineMain.main(args)

@Suppress("unused")
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false, koinModules: List<Module> = listOf(appModule)) {
    configureKoin(koinModules)
    setupConfig()
    configureLogging()
    configureSecurity()
    configureSerialization()
    configureSockets()
    configureRouting()
}
