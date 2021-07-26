package top.warmthdawn.emss.plugins

import io.ktor.application.*
import kotlinx.coroutines.runBlocking
import org.koin.ktor.ext.inject
import top.warmthdawn.emss.features.statistics.StatisticsService
import top.warmthdawn.emss.features.system.SystemService

/**
 *
 * @author WarmthDawn
 * @since 2021-07-19
 */
fun Application.initialize() {
    val statisticsService by inject<StatisticsService>()
    val systemService by inject<SystemService>()
    runBlocking {
        statisticsService.init()
        systemService.init()
    }



}