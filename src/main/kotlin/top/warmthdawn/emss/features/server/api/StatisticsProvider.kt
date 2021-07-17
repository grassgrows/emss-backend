package top.warmthdawn.emss.features.server.api

import kotlinx.coroutines.CoroutineScope
import top.warmthdawn.emss.features.server.entity.StatisticsInfo

/**
 *
 * @author WarmthDawn
 * @since 2021-07-17
 */
interface StatisticsProvider : CoroutineScope {
    fun getCurrent(): StatisticsInfo
    fun getHistory(): List<StatisticsInfo>
}