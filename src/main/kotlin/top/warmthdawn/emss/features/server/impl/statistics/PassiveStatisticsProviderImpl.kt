package top.warmthdawn.emss.features.server.impl

import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import top.warmthdawn.emss.features.server.entity.StatisticsType
import java.util.*
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.coroutines.CoroutineContext

/**
 *
 * @author WarmthDawn
 * @since 2021-07-17
 */
abstract class PassiveStatisticsProviderImpl(
    maxHistory: Int,
    type: StatisticsType,
    parentContext: CoroutineContext,
    abbr: String, delay: Long
) :
    AbstractStatisticsProvider(delay, maxHistory, type, parentContext, abbr) {


    abstract suspend fun beginListen(valueCallback: (Double) -> Unit): Double

    override fun start() {
        launch {
            val results = LinkedList<Double>()
            val lock = ReentrantLock()
            val valueTask = async {
                beginListen {
                    lock.lock()
                    results.add(it)
                    lock.unlock()
                }
            }

            val resultTask = async {
                while (true) {
                    lock.lock()
                    val result = results.reduce { a, b -> a + b }
                    results.clear()
                    lock.unlock()
                    offerHistory(result)
                    delay(delay)
                }
            }

            valueTask.await()
            resultTask.cancel()
            resultTask.await()

        }
    }

    override fun close() {
        coroutineContext.cancel()
    }
}