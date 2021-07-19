package top.warmthdawn.emss.features.server.impl

import io.ktor.network.util.*
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import top.warmthdawn.emss.features.server.entity.StatisticsType
import kotlin.coroutines.CoroutineContext

/**
 *
 * @author WarmthDawn
 * @since 2021-07-17
 */
abstract class ActiveStatisticsProviderImpl(
    maxHistory: Int,
    type: StatisticsType,
    parentContext: CoroutineContext,
    abbr: String, delay: Long
) : AbstractStatisticsProvider(delay, maxHistory, type, parentContext, abbr) {
    abstract suspend fun getInfo(): Double
    override fun start() {
        launch {
            while (true) {
                offerHistory(getInfo())
                delay(delay)
            }
        }
    }

    override fun close() {
        coroutineContext.cancel()
    }
}