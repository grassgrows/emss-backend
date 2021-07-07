package top.warmthdawn.emss.utils

import io.ktor.config.*
import io.ktor.server.testing.*
import org.koin.dsl.module
import top.warmthdawn.emss.config.AppConfig
import top.warmthdawn.emss.module
import org.koin.core.module.Module
import org.koin.dsl.single

fun MapApplicationConfig.createConfigForTesting() {
    // Server config
    put("ktor.server.isProd", "false")

    // Database Config
    put("ktor.database.driverClass", "org.h2.Driver")
    put("ktor.database.url", "jdbc:h2:mem:;DATABASE_TO_UPPER=false;MODE=MYSQL")
    put("ktor.database.user", "root")
    put("ktor.database.password", "password")
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
    single<AppConfig>()
}
