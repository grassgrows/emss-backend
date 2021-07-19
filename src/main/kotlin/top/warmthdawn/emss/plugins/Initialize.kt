package top.warmthdawn.emss.plugins

import io.ktor.application.*
import kotlinx.coroutines.runBlocking
import org.koin.ktor.ext.inject
import top.warmthdawn.emss.features.command.CommandService
import top.warmthdawn.emss.features.server.impl.ServerObjectFactory
import top.warmthdawn.emss.features.server.impl.StatisticsService

/**
 *
 * @author WarmthDawn
 * @since 2021-07-19
 */
fun Application.initialize() {
    val statisticsService by inject<StatisticsService>()
    val factory by inject<ServerObjectFactory>()
    runBlocking {
        factory.init()
        statisticsService.init()
    }



}