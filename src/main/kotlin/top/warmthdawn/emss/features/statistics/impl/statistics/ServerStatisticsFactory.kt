package top.warmthdawn.emss.features.statistics.impl.statistics

import kotlinx.coroutines.coroutineScope
import top.warmthdawn.emss.features.server.entity.StatisticsType
import top.warmthdawn.emss.features.statistics.impl.ServerStatistics

/**
 * 用于创建服务器状态监控
 * @author WarmthDawn
 * @since 2021-07-19
 */
class ServerStatisticsFactory(
    private val maxHistory: Int = 60,
    private val delay: Long = 60 * 1000
) {
    private suspend fun createPassiveProvider(abbr: String, type: StatisticsType) = coroutineScope {
        PassiveStatisticsProviderImpl(
            maxHistory,
            type,
            coroutineContext,
            abbr,
            delay
        ).also {
            it.start()
        }
    }

    private suspend fun createActiveProvider(abbr: String, type: StatisticsType) = coroutineScope {
        ActiveStatisticsProviderImpl(
            maxHistory,
            coroutineContext,
            type,
            abbr,
            delay
        ).also {
            it.start()
        }
    }


    suspend fun createService(abbr: String): ServerStatistics {
        return ServerStatistics(
            cpu = createPassiveProvider(abbr, StatisticsType.CPU),
            memory = createPassiveProvider(abbr, StatisticsType.MEMORY),
            networkDownload = createPassiveProvider(abbr, StatisticsType.NETWORK_DOWNLOAD),
            networkUpload = createPassiveProvider(abbr, StatisticsType.NETWORK_UPLOAD),
            diskRead = createPassiveProvider(abbr, StatisticsType.DISK_READ),
            diskWrite = createPassiveProvider(abbr, StatisticsType.DISK_WRITE),
            tps = createActiveProvider(abbr, StatisticsType.TPS),
            players = createActiveProvider(abbr, StatisticsType.ONLINE_PLAYER),
        )
    }

}


