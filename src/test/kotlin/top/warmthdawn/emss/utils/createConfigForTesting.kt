package top.warmthdawn.emss.utils

import io.ktor.config.*
import io.ktor.server.testing.*
import org.koin.dsl.module
import top.warmthdawn.emss.config.AppConfig
import top.warmthdawn.emss.module
import org.koin.core.module.Module
import org.koin.dsl.single
import top.warmthdawn.emss.database.DBFactory
import top.warmthdawn.emss.database.DBFactoryImpl
import top.warmthdawn.emss.features.docker.ImageDownloadScheduler
import top.warmthdawn.emss.features.server.ServerService
import top.warmthdawn.emss.features.settings.ImageService
import top.warmthdawn.emss.features.settings.SettingService

fun MapApplicationConfig.createConfigForTesting() {
    // Server config
    put("ktor.server.isProd", "false")

    // Database Config
    put("ktor.database.driverClass", "org.h2.Driver")
    put("ktor.database.url", "jdbc:h2:mem:test;DATABASE_TO_UPPER=false;MODE=MYSQL")
    put("ktor.database.maxPoolSize", "1")
}


fun withTestServer(koinModules: List<Module> = listOf(appTestModule), block: TestApplicationEngine.() -> Unit) {
    withTestApplication(
        {
            (environment.config as MapApplicationConfig).apply {
                createConfigForTesting()
            }
            module(testing = true, koinModules = koinModules)
        }, block
    )
}

val appTestModule = module {
    // Backend Config
    single<AppConfig>()
    single<DBFactory> { DBFactoryImpl(get()) }

    //setting
    single { SettingService(get(), get()) }
    single { ImageDownloadScheduler() }
    single { ImageService(get(), get(), get()) }

    //server
    single { ServerService(get(), get()) }
}
