package top.warmthdawn.emss.features.statistics.impl.statistics

import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import top.warmthdawn.emss.features.statistics.api.StatisticsProvider
import top.warmthdawn.emss.features.server.entity.StatisticsType
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.coroutines.CoroutineContext
import kotlin.math.sign

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
                    var result: Double
                    lock.lock()
                    try {
                        result = results.reduce { a, b -> a + b }
                        result /= results.size
                        results.clear()
                    }finally {
                        lock.unlock()
                    }
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