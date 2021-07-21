package top.warmthdawn.emss.features.statistics.impl.statistics

import io.ebean.Database
import kotlinx.coroutines.coroutineScope
import top.warmthdawn.emss.database.entity.query.QServer
import top.warmthdawn.emss.features.server.entity.StatisticsType
import top.warmthdawn.emss.features.statistics.impl.ServerStatistics
import top.warmthdawn.emss.features.statistics.minecraft.ping.MinecraftTools

/**
 * 用于创建服务器状态监控
 * @author WarmthDawn
 * @since 2021-07-19
 */
class ServerStatisticsFactory(
    private val db: Database,
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

    private suspend fun createActiveProvider(abbr: String, type: StatisticsType, func: () -> Double) = coroutineScope {
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


    suspend fun createService(id: Long, abbr: String): ServerStatistics {
        return ServerStatistics(
            cpu = createPassiveProvider(abbr, StatisticsType.CPU),
            memory = createPassiveProvider(abbr, StatisticsType.MEMORY),
            networkDownload = createPassiveProvider(abbr, StatisticsType.NETWORK_DOWNLOAD),
            networkUpload = createPassiveProvider(abbr, StatisticsType.NETWORK_UPLOAD),
            diskRead = createPassiveProvider(abbr, StatisticsType.DISK_READ),
            diskWrite = createPassiveProvider(abbr, StatisticsType.DISK_WRITE),
            tps = createActiveProvider(abbr, StatisticsType.TPS) { 20.0 },
            players = createActiveProvider(abbr, StatisticsType.ONLINE_PLAYER) {
                MinecraftTools.ping(QServer(db).id.eq(id).findOne()!!.portBindings.keys.first()).toDouble()
            },
        )
    }

}


