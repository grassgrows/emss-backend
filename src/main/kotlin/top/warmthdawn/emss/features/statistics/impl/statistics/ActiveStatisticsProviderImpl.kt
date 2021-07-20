package top.warmthdawn.emss.features.statistics.impl.statistics

import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import top.warmthdawn.emss.features.statistics.api.StatisticsProvider
import top.warmthdawn.emss.features.server.entity.StatisticsType
import kotlin.coroutines.CoroutineContext

/**
 *
 * @author WarmthDawn
 * @since 2021-07-17
 */
class ActiveStatisticsProviderImpl(
    maxHistory: Int,
    parentContext: CoroutineContext,
    type: StatisticsType,
    abbr: String, delay: Long,
    private val infoFunc: (() -> Double)? = null,
) : AbstractStatisticsProvider(delay, maxHistory, type, parentContext, abbr) {
    var enabled = false
    override fun start() {
        launch {
            while (true) {
                if (infoFunc != null && enabled) {
                    offerHistory(infoFunc.invoke())
                } else {
                    offerHistory(0.0)
                }
                delay(delay)
            }
        }
    }

    override fun close() {
        coroutineContext.cancel()
    }
}

val StatisticsProvider.active get() = this as ActiveStatisticsProviderImpl