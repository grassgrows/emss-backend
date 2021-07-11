package top.warmthdawn.emss

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.locations.*
import io.ktor.server.netty.*
import top.warmthdawn.emss.plugins.*
import top.warmthdawn.emss.config.setupConfig
import top.warmthdawn.emss.di.appModule
import org.koin.core.module.Module

fun main(args: Array<String>) = EngineMain.main(args)

@Suppress("unused")
fun Application.module(testing: Boolean = false, koinModules: List<Module> = listOf(appModule)) {
    configureKoin(koinModules)
    setupConfig(testing)
    configureDatabase()
    configureCors()
    configureLogging()
    configureSecurity()
    configureSerialization()
    install(Locations)
    install(ForwardedHeaderSupport)
    configureSockets()
    configureRouting()
}
