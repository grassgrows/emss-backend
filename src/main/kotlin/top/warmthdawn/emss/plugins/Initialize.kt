package top.warmthdawn.emss.plugins

import io.ktor.application.*
import kotlinx.coroutines.runBlocking
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import top.warmthdawn.emss.features.docker.DockerService
import top.warmthdawn.emss.features.server.ServerAutoRestartHandler
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
    val restartHandler by inject<ServerAutoRestartHandler>()
    val dockerService by inject<DockerService>()
    val logger = LoggerFactory.getLogger("System-Initialize")
    runBlocking {
        logger.info("初始化Docker容器中")
        dockerService.init()
        logger.info("初始化崩溃自动重启中")
        restartHandler.init()
        logger.info("初始化状态监控中")
        statisticsService.init()
        systemService.init()
    }



}