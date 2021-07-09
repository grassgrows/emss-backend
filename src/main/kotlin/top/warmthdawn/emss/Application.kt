package top.warmthdawn.emss

import io.ktor.application.*
import io.ktor.server.tomcat.*
import top.warmthdawn.emss.plugins.*
import top.warmthdawn.emss.config.setupConfig
import top.warmthdawn.emss.di.appModule
import org.koin.core.module.Module

fun main(args: Array<String>) = EngineMain.main(args)

@Suppress("unused")
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false, koinModules: List<Module> = listOf(appModule)) {
    configureKoin(koinModules)
    setupConfig(testing)
    configureDatabase()
    configureLogging()
    configureSecurity()
    configureSerialization()
    configureSockets()
    configureRouting()
}
