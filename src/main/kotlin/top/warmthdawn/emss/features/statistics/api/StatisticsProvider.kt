package top.warmthdawn.emss.features.statistics.api

import kotlinx.coroutines.CoroutineScope
import top.warmthdawn.emss.features.server.entity.StatisticsInfo
import top.warmthdawn.emss.features.server.entity.StatisticsList
import top.warmthdawn.emss.features.server.entity.StatisticsType
import java.io.Closeable

/**
 *
 * @author WarmthDawn
 * @since 2021-07-17
 */
interface StatisticsProvider : CoroutineScope, Closeable {
    fun getCurrent(): StatisticsInfo
    fun getHistory(): StatisticsList
    val type: StatisticsType
    fun start()
    override fun close()

}