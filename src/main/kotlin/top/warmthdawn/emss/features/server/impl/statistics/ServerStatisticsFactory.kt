package top.warmthdawn.emss.features.server.impl.statistics

import io.ebean.Database
import top.warmthdawn.emss.features.server.impl.ServerStatistics
import top.warmthdawn.emss.features.server.impl.StatisticsService

/**
 * 用于创建服务器状态监控
 * @author WarmthDawn
 * @since 2021-07-19
 */
class ServerStatisticsFactory(
    private val db: Database
) {
    fun createService(id: Long): ServerStatistics {

    }
}


