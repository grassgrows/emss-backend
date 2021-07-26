package top.warmthdawn.emss.features.statistics

import io.ebean.Database
import org.slf4j.LoggerFactory
import top.warmthdawn.emss.database.entity.Server
import top.warmthdawn.emss.database.entity.ServerStatistics
import top.warmthdawn.emss.database.entity.query.QServer
import top.warmthdawn.emss.database.entity.query.QServerRealTime
import top.warmthdawn.emss.database.entity.query.QServerStatistics
import top.warmthdawn.emss.features.docker.ContainerStatus
import top.warmthdawn.emss.features.docker.DockerManager
import top.warmthdawn.emss.features.docker.DockerService
import top.warmthdawn.emss.features.server.entity.StatisticsType
import top.warmthdawn.emss.features.server.vo.ServerStatisticsVO
import top.warmthdawn.emss.features.statistics.minecraft.ping.MinecraftTools
import kotlin.concurrent.fixedRateTimer

/**
 *
 * @author WarmthDawn
 * @since 2021-07-19
 */
class StatisticsService(
    private val dockerService: DockerService,
    private val db: Database
) {

    companion object {
        private val logger = LoggerFactory.getLogger(StatisticsService::class.java)
    }

    fun init() {
        fixedRateTimer("statistics_timer", true, 0, 60 * 1000L) {
            QServer().findList().forEach {
                tick(it)
            }
        }
    }

    private fun tickContainer(serverId: Long, containerId: String) {
        try {
            val result = DockerManager.statsContainer(containerId)
            val time = System.currentTimeMillis() / 1000

            db.insertAll(
                listOf(
                    ServerStatistics(serverId, StatisticsType.CPU, time, result.cpuPercent),
                    ServerStatistics(serverId, StatisticsType.MEMORY, time, result.currentMemory),
                    ServerStatistics(serverId, StatisticsType.DISK_WRITE, time, result.diskWrite),
                    ServerStatistics(serverId, StatisticsType.DISK_READ, time, result.diskRead),
                    ServerStatistics(serverId, StatisticsType.NETWORK_UPLOAD, time, result.uploadBytes),
                    ServerStatistics(serverId, StatisticsType.NETWORK_DOWNLOAD, time, result.downloadBytes),
                )
            )
        } catch (e: Exception) {
            logger.error("获取服务器${serverId}信息失败", e)
        }


    }

    private fun tickMinecraft(serverId: Long, port: Int) {
        val ping = MinecraftTools.ping(port)
        if (ping != null) {
            val time = System.currentTimeMillis() / 1000
            ServerStatistics(serverId, StatisticsType.ONLINE_PLAYER, time, ping.playerOnline)
                .update()
            QServerRealTime().id.eq(serverId).findOne()?.apply {
                serverMaxPlayer = ping.playerMax
                serverPlayerNumber = ping.playerOnline
                update()
            }
        }
        val timeTps = System.currentTimeMillis() / 1000
        //TODO: TPS
        ServerStatistics(serverId, StatisticsType.TPS, timeTps, 20).insert()

    }

    fun tick(server: Server) {
        if (server.containerId == null) {
            return
        }

        try {
            val status = DockerManager.inspectContainer(server.containerId)
            if (status.status != ContainerStatus.Running) {
                return
            }
        } catch (e: Exception) {
            return
        }

        tickContainer(server.id!!, server.containerId!!)
        tickMinecraft(server.id!!, server.hostPort)
        removeStatistics()
    }


    fun removeStatistics() {

    }

    fun getStatistics(statisticsType: StatisticsType): ServerStatisticsVO {
        val statistics = QServerStatistics(db)
            .type.eq(statisticsType)
            .orderBy().time.asc()
            .findList()

        return ServerStatisticsVO(
            statisticsType,
            statistics.map { it.time },
            statistics.lastOrNull()?.value ?: 0.0,
            statistics.map { it.value },
        )
    }

    fun getStatistics(statisticsTypes: List<StatisticsType>): List<ServerStatisticsVO> {
        return QServerStatistics(db)
            .type.isIn(statisticsTypes)
            .findList()
            .groupBy { it.type }
            .map { (statisticsType, statistics) ->
                val sorted = statistics.sortedBy { it.time }
                ServerStatisticsVO(
                    statisticsType,
                    sorted.map { it.time },
                    sorted.lastOrNull()?.value ?: 0.0,
                    sorted.map { it.value },
                )
            }

    }
}

class ServerNotFoundException(message: String) : RuntimeException(message)


