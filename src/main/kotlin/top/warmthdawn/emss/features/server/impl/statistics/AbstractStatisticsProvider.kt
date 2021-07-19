package top.warmthdawn.emss.features.server.impl

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import top.warmthdawn.emss.features.server.api.StatisticsProvider
import top.warmthdawn.emss.features.server.entity.StatisticsInfo
import top.warmthdawn.emss.features.server.entity.StatisticsList
import top.warmthdawn.emss.features.server.entity.StatisticsType
import java.util.*
import kotlin.coroutines.CoroutineContext

/**
 *
 * @author WarmthDawn
 * @since 2021-07-17
 */
abstract class AbstractStatisticsProvider(
    val delay: Long,
    val maxHistory: Int,
    val type: StatisticsType,
    parentContext: CoroutineContext,
    abbr: String,
) : StatisticsProvider, CoroutineScope {
    private val job = Job()
    override fun getCurrent(): StatisticsInfo {
        return StatisticsInfo(type, historyQueue.lastOrNull()?.second ?: 0.0)
    }

    override fun getHistory(): StatisticsList {
        val (timestamps, values) = historyQueue.toList().unzip()
        return StatisticsList(type, timestamps, values)
    }

    private val historyQueue = LinkedList<Pair<Long, Double>>()
    protected fun offerHistory(value: Double) {
        if (historyQueue.size > maxHistory) {
            historyQueue.pop()
        }
        historyQueue.offer(Pair(System.currentTimeMillis() / 100, value))
    }

    abstract fun start()
    abstract fun close()

    override val coroutineContext: CoroutineContext =
        CoroutineName("STATUS_PROVIDER($abbr)")
            .plus(parentContext)
            .plus(job)


}
