package top.warmthdawn.emss.plugins

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.core.module.Module
import org.koin.ktor.ext.Koin
import org.koin.logger.slf4jLogger

/**
 *
 * @author WarmthDawn
 * @since 2021-07-07
 */
fun Application.configureKoin(koinModules: List<Module>) {
    install(Koin) {
        slf4jLogger()
        modules(koinModules)
    }
}
