package top.warmthdawn.emss.utils

import com.fasterxml.jackson.databind.ObjectMapper
import io.ebean.Database
import io.ebean.annotation.TxIsolation
import io.ktor.config.*
import io.ktor.server.testing.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.dsl.module
import top.warmthdawn.emss.config.AppConfig
import top.warmthdawn.emss.module
import org.koin.core.module.Module
import org.koin.dsl.single
import org.koin.ktor.ext.inject
import top.warmthdawn.emss.database.DBFactory
import top.warmthdawn.emss.database.DBFactoryImpl
import top.warmthdawn.emss.features.command.CommandService
import top.warmthdawn.emss.features.docker.DockerService
import top.warmthdawn.emss.features.docker.ImageDownloadScheduler
import top.warmthdawn.emss.features.file.FileService
import top.warmthdawn.emss.features.login.LoginService
import top.warmthdawn.emss.features.permission.PermissionService
import top.warmthdawn.emss.features.server.ServerService
import top.warmthdawn.emss.features.server.impl.ServerObjectFactory
import top.warmthdawn.emss.features.server.impl.StatisticsService
import top.warmthdawn.emss.features.server.impl.statistics.ServerStatisticsFactory
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


fun withTestServer(useMemoryDB: Boolean = true, koinModules: List<Module> = listOf(appTestModule), block: suspend TestApplicationEngine.() -> Unit) {
    withTestApplication(
        {
            (environment.config as MapApplicationConfig).apply {
                createConfigForTesting()
                put("test.useMemoryDB", useMemoryDB.toString())
            }
            module(testing = true, koinModules = koinModules)
        }, {
            val db by application.inject<Database>()
            db.beginTransaction(TxIsolation.SERIALIZABLE)
            try {
                runBlocking {
                    block()
                }
            }finally {
                db.rollbackTransaction()
            }
        }
    )
}
private val _json = ObjectMapper()
val TestApplicationEngine.json: ObjectMapper
    get() = _json

val appTestModule = module {
    // Backend Config
    single<AppConfig>()
    single<DBFactory> { DBFactoryImpl(get()) }

    //setting
    single { SettingService(get(), get(), get(), get()) }
    single { ImageDownloadScheduler(get()) }
    single { ImageService(get(), get(), get()) }

    //server
    single { ServerService(get(), get(), get(), get(), get(), get()) }
    single { ServerObjectFactory(get(), get(), get(), get()) }
    single { DockerService(get(), get()) }
    //status
    single { StatisticsService(get(), get(), get()) }
    single { ServerStatisticsFactory() }
    //file
    single { FileService() }


    //command
    single { CommandService() }

    //permission
    single { PermissionService(get()) }


    single { LoginService(get()) }
    single { CommandService(get()) }
}
