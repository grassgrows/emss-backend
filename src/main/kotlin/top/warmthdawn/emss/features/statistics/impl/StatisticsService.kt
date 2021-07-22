package top.warmthdawn.emss.features.statistics.impl

import io.ebean.Database
import top.warmthdawn.emss.database.entity.query.QServer
import top.warmthdawn.emss.features.docker.DockerManager
import top.warmthdawn.emss.features.docker.DockerService
import top.warmthdawn.emss.features.server.entity.StatisticsType
import top.warmthdawn.emss.features.server.entity.contains
import top.warmthdawn.emss.features.statistics.api.StatisticsProvider
import top.warmthdawn.emss.features.statistics.impl.statistics.ServerStatisticsFactory
import top.warmthdawn.emss.features.statistics.impl.statistics.active
import top.warmthdawn.emss.features.statistics.impl.statistics.passive
import java.io.Closeable

/**
 *
 * @author WarmthDawn
 * @since 2021-07-19
 */
class StatisticsService(
    private val factory: ServerStatisticsFactory,
    private val dockerService: DockerService,
    private val db: Database
) {
    private val providers = mutableMapOf<Long, ServerStatistics>()

    fun getProvider(id: Long): ServerStatistics {
        return providers[id] ?: throw ServerNotFoundException("服务器信息不存在")
    }

    fun addServer(id: Long, statistics: ServerStatistics) {
        providers[id] = statistics
    }

    suspend fun addServer(id: Long, abbr: String) {
        providers[id] = factory.createService(id, abbr)
    }

    suspend fun init() {
        val servers = QServer(db).findList()
        servers.forEach {
            addServer(it.id!!, it.abbr)
        }

    }

    fun delServer(id: Long) {
        providers[id]?.clearMonitor()
        providers[id]?.close()
        providers.remove(id)
    }

    fun startMonitoring(id: Long) {
        val containerId = dockerService.getContainerId(id)
        getProvider(id).apply {
            addMonitor(DockerManager.statsContainer(containerId) {
                //CPU
                val cpuDelta = (it.cpuStats.cpuUsage?.totalUsage ?: 0) - (it.preCpuStats.cpuUsage?.totalUsage ?: 0)
                val systemCpuDelta = (it.cpuStats.systemCpuUsage ?: 0) - (it.preCpuStats.systemCpuUsage ?: 0)
                val numberCpus = it.cpuStats.onlineCpus ?: it.cpuStats.cpuUsage?.percpuUsage?.size ?: 0
                val cpuPercent = (cpuDelta * 1.0 / systemCpuDelta) * numberCpus.toDouble() * 100.0
                cpu.passive.onResult(cpuPercent)

                //内存
                val memoryUsage =
                    (it.memoryStats.usage?.toDouble() ?: 0.0) - (it.memoryStats.stats?.cache?.toDouble() ?: 0.0)
                memory.passive.onResult(memoryUsage)


                //磁盘
                var diskWriteValue = 0.0
                var diskReadValue = 0.0
                it.blkioStats?.ioServiceBytesRecursive?.forEach { entry ->
                    when (entry.op.lowercase()) {
                        "read" -> {
                            diskReadValue += entry.value
                        }
                        "write" -> {
                            diskWriteValue += entry.value
                        }
                    }
                }
                diskRead.passive.onResult(diskReadValue)
                diskWrite.passive.onResult(diskWriteValue)

                //网络
                val networksIn = it.networks?.values?.asSequence()
                    ?.map { net -> net.rxBytes }
                    ?.filterNotNull()
                    ?.reduce { a, b -> a + b }
                    ?.toDouble() ?: 0.0
                networkDownload.passive.onResult(networksIn)


                val netWorksOut = it.networks?.values?.asSequence()
                    ?.map { net -> net.txBytes }
                    ?.filterNotNull()
                    ?.reduce { a, b -> a + b }
                    ?.toDouble() ?: 0.0
                networkUpload.passive.onResult(netWorksOut)

            })
            addMonitor({
                this.players.active.enabled = false
                this.tps.active.enabled = false
            }) {
                this.players.active.enabled = true
                this.tps.active.enabled = true
            }
        }

    }

    fun stopMonitoring(id: Long) {
        getProvider(id).clearMonitor()
    }
}

class ServerNotFoundException(message: String) : RuntimeException(message)

/**
 * 每个服务器的信息
 */
class ServerStatistics(
    val cpu: StatisticsProvider,
    val memory: StatisticsProvider,
    val networkUpload: StatisticsProvider,
    val networkDownload: StatisticsProvider,
    val diskWrite: StatisticsProvider,
    val diskRead: StatisticsProvider,
    val tps: StatisticsProvider,
    val players: StatisticsProvider,

    ) : Closeable {

    private val monitors = mutableListOf<Closeable>()

    val all get() = listOf(cpu, memory, networkDownload, networkUpload, diskRead, diskWrite, tps, players)

    fun getByFlag(flags: Int): List<StatisticsProvider> {
        return all.filter {
            it.type in flags
        }
    }

    fun getByType(statisticsType: StatisticsType): StatisticsProvider {
        return all.first {
            it.type == statisticsType
        }
    }

    fun addMonitor(closeable: Closeable) {
        monitors.add(closeable)
    }

    fun addMonitor(onStop: () -> Unit = {}, onStart: () -> Unit) {
        onStart()
        monitors.add(onStop)
    }

    fun clearMonitor() {
        val it = monitors.iterator()
        while (it.hasNext()) {
            it.next().close()
            it.remove()
        }
    }

    override fun close() {
        clearMonitor()
        all.forEach { it.close() }
    }
}

