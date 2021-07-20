package top.warmthdawn.emss.features.server.impl.statistics

import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import top.warmthdawn.emss.features.server.api.StatisticsProvider
import top.warmthdawn.emss.features.server.entity.StatisticsType
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.coroutines.CoroutineContext

/**
 *
 * @author WarmthDawn
 * @since 2021-07-17
 */
class PassiveStatisticsProviderImpl(
    maxHistory: Int,
    type: StatisticsType,
    parentContext: CoroutineContext,
    abbr: String, delay: Long
) :
    AbstractStatisticsProvider(delay, maxHistory, type, parentContext, abbr) {


    /**
     * 像Provider提供数据
     */
    fun onResult(double: Double) {
        lock.lock()
        results.add(double)
        lock.unlock()
    }

    private val results = LinkedList<Double>()
    private val lock = ReentrantLock()
    override fun start() {
        launch {
            while (true) {
                if (results.isEmpty()) {
                    offerHistory(0.0)
                } else {
                    lock.lock()
                    val result = results.reduce { a, b -> a + b }
                    results.clear()
                    lock.unlock()
                    offerHistory(result)
                }
                delay(delay)
            }
        }
    }

    override fun close() {
        coroutineContext.cancel()
    }
}


val StatisticsProvider.passive get() = this as PassiveStatisticsProviderImpl