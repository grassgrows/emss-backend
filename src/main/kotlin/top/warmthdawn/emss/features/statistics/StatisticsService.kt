package top.warmthdawn.emss.features.statistics

import io.ebean.Database
import org.bouncycastle.crypto.tls.ConnectionEnd.client
import org.slf4j.LoggerFactory
import top.limbang.doctor.allLoginPlugin.enableAllLoginPlugin
import top.limbang.doctor.client.MinecraftClient
import top.limbang.doctor.client.MinecraftClientBuilder
import top.limbang.doctor.client.running.AutoVersionForgePlugin
import top.limbang.doctor.client.running.TpsPlugin
import top.limbang.doctor.network.event.ConnectionEvent
import top.warmthdawn.emss.database.entity.Server
import top.warmthdawn.emss.database.entity.ServerStatistics
import top.warmthdawn.emss.database.entity.query.QServer
import top.warmthdawn.emss.database.entity.query.QServerRealTime
import top.warmthdawn.emss.database.entity.query.QServerStatistics
import top.warmthdawn.emss.features.docker.DockerManager
import top.warmthdawn.emss.features.docker.DockerService
import top.warmthdawn.emss.features.server.entity.StatisticsType
import top.warmthdawn.emss.features.server.vo.ServerStatisticsVO
import top.warmthdawn.emss.features.statistics.StatisticsService.Companion.mcBotList
import top.warmthdawn.emss.features.statistics.minecraft.ping.MinecraftTools
import java.time.Clock.tick
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
        private val mcBotList = mutableMapOf<Long, MinecraftClient>()
        private val unSupportList = mutableListOf<Long>()
        private val logger = LoggerFactory.getLogger(StatisticsService::class.java)
        private fun mcBotBuilder(port: Int): MinecraftClient? {
            val client = MinecraftClient.builder()
                .user("1304793916@qq.com", "1234567890")
                .authServerUrl("https://skin.blackyin.xyz/api/yggdrasil/authserver")
                .sessionServerUrl("https://skin.blackyin.xyz/api/yggdrasil/sessionserver")
                .plugin(TpsPlugin())
                .plugin(AutoVersionForgePlugin())
                .enableAllLoginPlugin()
                .build()
            if (!client.start("localhost", port)) return null
            return client
        }
    }

    fun init() {
        mcBotList.forEach { (_, client) ->
            client.on(ConnectionEvent.Disconnect) {
                Thread.sleep(1000L)
                client.reconnect()
            }
        }
        var time = 0
        fixedRateTimer("botListener", true, 0, 30 * 60 * 1000L) {
            if (time != 0) {
                Thread.sleep(5000L)
                mcBotList.forEach { (_, client) ->
                    client.stop()
                }
                mcBotList.clear()
                QServer().findList().forEach {
                    if (dockerService.isRunning(it.id!!) && !unSupportList.contains(it.id)) {
                        val client = mcBotBuilder(it.hostPort)
                        if (client != null) mcBotList[it.id!!] = client
                    }
                }
            } else time = 1
        }
        fixedRateTimer("statistics_timer", true, 0, 10 * 1000L) {
            val serverList = QServer().findList()
            serverList.forEach {
                tick(it)
                if (dockerService.isRunning(it.id!!) && mcBotList[it.id!!] == null && !unSupportList.contains(it.id)) {
                    val client = mcBotBuilder(it.hostPort)
                    if (client != null) mcBotList[it.id!!] = client
                }
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
                .insert()
            QServerRealTime().id.eq(serverId).findOne()?.apply {
                serverMaxPlayer = ping.playerMax
                serverPlayerNumber = ping.playerOnline
                update()
            }
            if (ping.versionNumber != 340) {
                unSupportList.removeAll(listOf(serverId))
                unSupportList.add(serverId)
            }
        } else {
            mcBotList[serverId]?.stop()
            mcBotList.remove(serverId)
        }
        val timeTps = System.currentTimeMillis() / 1000
        //TODO: TPS
        val client = mcBotList[serverId]
        val tps: Double = if (client != null) {
            MinecraftTools.tps(client) ?: 0.0
        } else 0.0

        ServerStatistics(serverId, StatisticsType.TPS, timeTps, tps).insert()

    }

    fun tick(server: Server) {
        if (server.containerId == null) {
            return
        }

        if (!dockerService.isRunning(server.id!!)) {
            return
        }

        tickContainer(server.id!!, server.containerId!!)
        tickMinecraft(server.id!!, server.hostPort)
        removeStatistics()
    }


    fun removeStatistics() {
        val begin = System.currentTimeMillis() / 1000 - 60 * 61
        QServerStatistics().time.lt(begin).delete()
    }

    fun getStatistics(statisticsType: StatisticsType): ServerStatisticsVO {
        val statistics = QServerStatistics(db)
            .type.eq(statisticsType)
            .orderBy().time.asc()
            .findList()


        val (timestamps, values) = processStatistics(statistics)
        return ServerStatisticsVO(
            statisticsType,
            timestamps,
            statistics.lastOrNull()?.value ?: 0.0,
            values,
        )
    }

    private fun processStatistics(statistics: List<ServerStatistics>): Pair<MutableList<Long>, MutableList<Double>> {
        val timestamps = mutableListOf<Long>()
        val values = mutableListOf<Double>()
        var i = 0
        val now = System.currentTimeMillis() / 1000
        for (t in 3600 downTo 0 step 60) {
            val time = now - t
            var count = 0
            var avg = 0.0
            while (i < statistics.size && statistics[i].time <= time) {
                count++
                avg += statistics[i].value
                i++
            }
            if (count > 0) {
                avg /= count
            }
            timestamps.add(time)
            values.add(avg)
        }
        return Pair(timestamps, values)
    }

    fun getStatistics(statisticsTypes: List<StatisticsType>): List<ServerStatisticsVO> {
        return QServerStatistics(db)
            .type.isIn(statisticsTypes)
            .findList()
            .groupBy { it.type }
            .map { (statisticsType, statistics) ->
                val sorted = statistics.sortedBy { it.time }

                val (timestamps, values) = processStatistics(sorted)
                ServerStatisticsVO(
                    statisticsType,
                    timestamps,
                    sorted.lastOrNull()?.value ?: 0.0,
                    values,
                )
            }

    }
}

class ServerNotFoundException(message: String) : RuntimeException(message)


